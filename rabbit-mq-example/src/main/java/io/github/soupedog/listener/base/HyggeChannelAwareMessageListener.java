package io.github.soupedog.listener.base;

import com.rabbitmq.client.Channel;
import hygge.commons.constant.ConstantParameters;
import io.github.soupedog.listener.base.definition.HyggeListenerFeature;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.boot.logging.LogLevel;

import java.nio.charset.StandardCharsets;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public abstract class HyggeChannelAwareMessageListener<T> implements HyggeListenerFeature<T>, ChannelAwareMessageListener {
    protected String listenerName;
    protected String environmentName;
    protected long requeueToTailMillisecondInterval = 500L;
    protected int maxRequeueTimes = 1000;
    protected static String HEADERS_KEY_ENVIRONMENT_NAME = DEFAULT_HEADERS_KEY_ENVIRONMENT_NAME;
    protected static String HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER = DEFAULT_HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER;

    public HyggeChannelAwareMessageListener(String listenerName, String environmentName) {
        this.listenerName = listenerName;
        this.environmentName = environmentName;
    }

    @Override
    public String getListenerName() {
        return listenerName;
    }

    @Override
    public void onMessage(Message message, Channel channel) {
        HyggeRabbitMqListenerContext<Message> context = new HyggeRabbitMqListenerContext<>();
        try {
            onMessageMainLogic(message, channel, context);
        } finally {
            finallyHook(context);
        }
    }

    protected void onMessageMainLogic(Message message, Channel channel, HyggeRabbitMqListenerContext<Message> context) {
        context.setRwaMessage(message);
        context.setChannel(channel);

        String headersStringVal = null;
        String messageStringVal = null;

        try {
            // 是否忽略当前消息，并丢回队列尾部
            if (isRequeueEnable(context)) {
                requeue(context);
                return;
            }
        } catch (Exception e) {
            headersStringVal = formatMessageHeadersAsString(context);
            messageStringVal = formatMessageBodyAsString(context);

            // 日志对象覆写
            headersStringVal = messageHeadersOverwrite(context, headersStringVal, message.getMessageProperties().getHeaders());
            messageStringVal = messageBodyOverwrite(context, messageStringVal, null);

            String prefixInfo = String.format("HyggeListener(%s) fail to requeue, and this message turns into a unAcked message.", getListenerName());
            context.setLoglevel(LogLevel.ERROR);
            printMessageEntityLog(context, prefixInfo, headersStringVal, messageStringVal);
            return;
        }

        try {
            headersStringVal = formatMessageHeadersAsString(context);
            messageStringVal = formatMessageBodyAsString(context);

            T messageEntity = formatMessageAsEntity(context, messageStringVal);

            // 日志对象覆写
            headersStringVal = messageHeadersOverwrite(context, headersStringVal, message.getMessageProperties().getHeaders());
            messageStringVal = messageBodyOverwrite(context, messageStringVal, messageEntity);

            String prefixInfo = String.format("HyggeListener(%s) received message.", getListenerName());
            printMessageEntityLog(context, prefixInfo, headersStringVal, messageStringVal);

            onReceive(context, messageEntity);
        } catch (Exception e) {
            // 将异常存入上下文
            context.setThrowable(e);
            context.setRetryable(false);
        } finally {
            // 上下文中存在异常的统一会在此处处理
            autoAck(context, headersStringVal, messageStringVal);

            try {
                // 如果未抛出异常且未进行重试，则激活 businessLogicFinishHook 环节
                if (context.isNoExceptionOccurred()) {
                    context.setBusinessLogicFinishEnable(!retryHook(context));
                }
            } catch (Exception e) {
                String prefixInfo = String.format("HyggeListener(%s) fail to execute retryHook, and the finishHook method is automatically disabled.", getListenerName());
                context.setLoglevel(LogLevel.ERROR);
                printMessageEntityLog(context, prefixInfo, headersStringVal, messageStringVal);

                // 将异常存入上下文
                context.setThrowable(e);
            }

            if (context.isBusinessLogicFinishEnable()) {
                try {
                    businessLogicFinishHook(context);
                } catch (Exception e) {
                    String prefixInfo = String.format("HyggeListener(%s) fail to execute businessLogicFinishHook.", getListenerName());
                    context.setLoglevel(LogLevel.ERROR);
                    printMessageEntityLog(context, prefixInfo, headersStringVal, messageStringVal);

                    // 将异常存入上下文
                    context.setThrowable(e);
                }
            }
        }
    }

    @Override
    public boolean isRequeueEnable(HyggeRabbitMqListenerContext<Message> context) {
        String messageEnvironmentName = getValueFromHeaders(context, context.getRwaMessage(), HEADERS_KEY_ENVIRONMENT_NAME, true);

        return !environmentName.equals(messageEnvironmentName) && parameterHelper.isNotEmpty(messageEnvironmentName);
    }

    @Override
    public void requeue(HyggeRabbitMqListenerContext<Message> context) throws Exception {
        try {
            ack(context);

            // 防止重新投递过于迅速
            Thread.sleep(requeueToTailMillisecondInterval);

            // 当前消息重新发送到队尾
            Channel channel = context.getChannel();
            Message message = context.getRwaMessage();

            int requeueCounter = parameterHelper.integerFormatOfNullable(HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER, getValueFromHeaders(context, message, HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER, true), 0);
            requeueToTail(channel, message, HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER, requeueCounter, maxRequeueTimes);
        } catch (Exception e) {
            // 负反馈机制，防止加剧 ack/nack 不正常，不许重试
            context.setRetryable(false);
            throw e;
        }
    }

    @Override
    public String formatMessageHeadersAsString(HyggeRabbitMqListenerContext<Message> context) {
        return jsonHelper.formatAsString(context.getRwaMessage().getMessageProperties().getHeaders());
    }

    @Override
    public String formatMessageBodyAsString(HyggeRabbitMqListenerContext<Message> context) {
        return new String(context.getRwaMessage().getBody(), StandardCharsets.UTF_8);
    }

    @Override
    public void printMessageEntityLog(HyggeRabbitMqListenerContext<Message> context, String prefixInfo, String headersStringVal, String messageStringVal) {
        String logInfo = String.format("%s%sheaders:%s%sbody:%s",
                prefixInfo,
                ConstantParameters.LINE_SEPARATOR,
                headersStringVal,
                ConstantParameters.LINE_SEPARATOR,
                messageStringVal);

        printLog(context.getLoglevel(), logInfo);
    }

    @Override
    public void autoAck(HyggeRabbitMqListenerContext<Message> context, String headersStringVal, String messageStringVal) {
        try {
            // 自动 ack
            if (!context.isAutoAckTriggered()) {
                if (context.isNoExceptionOccurred()) {
                    ack(context);
                } else {
                    nack(context);

                    String prefixInfo = String.format("HyggeListener(%s) fail to consume, and this message was discarded.", getListenerName());
                    context.setLoglevel(LogLevel.ERROR);
                    printMessageEntityLog(context, prefixInfo, headersStringVal, messageStringVal);
                }
            }
        } catch (Exception e) {
            String ackInfo = context.isNoExceptionOccurred() ? "ack" : "nack";

            String prefixInfo = String.format("HyggeListener(%s) fail to auto %s, and this message turns into a unAcked message.", getListenerName(), ackInfo);
            context.setLoglevel(LogLevel.ERROR);
            printMessageEntityLog(context, prefixInfo, headersStringVal, messageStringVal);

            // 将异常存入上下文
            context.setThrowable(e);
        }
    }
}
