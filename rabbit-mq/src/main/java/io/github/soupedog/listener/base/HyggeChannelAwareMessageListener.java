package io.github.soupedog.listener.base;

import com.rabbitmq.client.Channel;
import hygge.commons.constant.ConstantParameters;
import hygge.commons.exception.InternalRuntimeException;
import io.github.soupedog.listener.base.definition.HyggeListenerFeature;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.boot.logging.LogLevel;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public abstract class HyggeChannelAwareMessageListener<T> implements HyggeListenerFeature<T>, ChannelAwareMessageListener {
    protected String listenerName;
    protected String environmentName;
    protected long requeueToTailMillisecondInterval = 500L;
    protected int maxRequeueTimes = 500;
    protected static String headersKeyEnvironmentName = DEFAULT_HEADERS_KEY_ENVIRONMENT_NAME;
    protected static String headersKeyRequeueToTailCounter = DEFAULT_HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER;

    protected HyggeChannelAwareMessageListener(String listenerName, String environmentName) {
        this.listenerName = listenerName;
        this.environmentName = environmentName;
    }

    @Override
    public String getListenerName() {
        return listenerName;
    }

    @Override
    public void onMessage(Message message, Channel channel) {
        HyggeRabbitMqListenerContext<T> context = new HyggeRabbitMqListenerContext<>();
        try {
            onMessageMainLogic(message, channel, context);
        } finally {
            finallyHook(context);
        }
    }

    protected void onMessageMainLogic(Message message, Channel channel, HyggeRabbitMqListenerContext<T> context) {
        context.setRwaMessage(new HyggeRabbitMQMessageItem<T>(message));
        context.setChannel(channel);

        String headersStringVal;
        String messageStringVal;

        HyggeRabbitMQMessageItem<T> messageItem = context.getRwaMessage();

        try {
            // 是否忽略当前消息，并需要尝试恢复到消费前等效的状态
            if (isRequeueEnable(context)) {
                requeue(context);
                return;
            }
        } catch (Exception e) {
            messageItem.setStatus(StatusEnums.REQUEUE_FAILURE);

            if (e instanceof InternalRuntimeException) {
                // 其实是超出 requeue 次数上限异常，不需要异常堆栈信息
                context.setLoglevelIntelligently(LogLevel.WARN);
            } else {
                // 将异常存入上下文
                messageItem.setException(e);
                context.setLoglevelIntelligently(LogLevel.ERROR);
            }

            headersStringVal = formatHeadersAsString(context, messageItem.getMessage().getMessageProperties().getHeaders());
            messageStringVal = formatBodyAsString(context, messageItem.getMessage());
            messageItem.setHeadersStringVal(headersStringVal);
            messageItem.setMessageStringVal(messageStringVal);

            // 日志对象覆写
            messageHeadersOverwrite(context);
            messageBodyOverwrite(context);

            String prefixInfo = String.format("HyggeListener(%s): Message(%s) failed to requeue.", getListenerName(), messageItem.getStatus().toString());
            printMessageEntityLog(context, prefixInfo);
            return;
        }

        try {
            headersStringVal = formatHeadersAsString(context, messageItem.getMessage().getMessageProperties().getHeaders());
            messageStringVal = formatBodyAsString(context, messageItem.getMessage());
            messageItem.setHeadersStringVal(headersStringVal);
            messageItem.setMessageStringVal(messageStringVal);

            T messageEntity = formatAsEntity(context, messageStringVal);
            messageItem.setMessageEntity(messageEntity);

            // 日志对象覆写
            messageHeadersOverwrite(context);
            messageBodyOverwrite(context);

            String prefixInfo = String.format("HyggeListener(%s): Received message.", getListenerName());
            printMessageEntityLog(context, prefixInfo);

            onReceive(context, messageEntity);
        } catch (Exception e) {
            // 将异常存入上下文
            messageItem.setException(e);
            context.setLoglevelIntelligently(LogLevel.ERROR);
        } finally {
            // 上下文中存在异常的统一会在此处处理
            autoAck(context);

            if (messageItem.statusExpected(StatusEnums.ACK_SUCCESS, StatusEnums.NACK_SUCCESS)) {
                try {
                    businessLogicFinishHook(context);
                } catch (Exception e) {
                    // 将异常存入上下文
                    messageItem.setException(e);
                    context.setLoglevelIntelligently(LogLevel.ERROR);

                    String prefixInfo = String.format("HyggeListener(%s): Message(%s) failed to execute businessLogicFinishHook.", getListenerName(), messageItem.getStatus().toString());
                    printMessageEntityLog(context, prefixInfo);
                }
            }

            try {
                if (messageItem.statusExpected(StatusEnums.NEEDS_RETRY)) {
                    retryHook(context);
                }
            } catch (Exception e) {
                // 将异常存入上下文
                messageItem.setException(e);
                context.setLoglevelIntelligently(LogLevel.ERROR);

                String prefixInfo = String.format("HyggeListener(%s): Message(%s) failed to execute retryHook.", getListenerName(), messageItem.getStatus().toString());
                printMessageEntityLog(context, prefixInfo);
            }
        }
    }

    @Override
    public boolean isRequeueEnable(HyggeRabbitMqListenerContext<T> context) throws Exception {
        HyggeRabbitMQMessageItem<T> messageItem = context.getRwaMessage();

        String messageEnvironmentName = getValueFromHeaders(messageItem, headersKeyEnvironmentName, true);

        if (!environmentName.equals(messageEnvironmentName) && parameterHelper.isNotEmpty(messageEnvironmentName)) {
            messageItem.setStatus(StatusEnums.NEEDS_REQUEUE);
            return true;
        }
        return false;
    }

    @Override
    public void requeue(HyggeRabbitMqListenerContext<T> context) throws Exception {
        HyggeRabbitMQMessageItem<T> messageItem = context.getRwaMessage();
        ack(context, messageItem);

        if (!messageItem.statusExpected(StatusEnums.ACK_SUCCESS)) {
            throw messageItem.getException();
        } else {
            messageItem.setStatus(StatusEnums.REQUEUE_HALF_SUCCESS);
        }

        // 防止重新投递过于迅速
        Thread.sleep(requeueToTailMillisecondInterval);

        // 当前消息重新发送到队尾
        Channel channel = context.getChannel();
        Message message = messageItem.getMessage();

        int requeueCounter = parameterHelper.integerFormatOfNullable(headersKeyRequeueToTailCounter, getValueFromHeaders(messageItem, headersKeyRequeueToTailCounter, true), 0);
        requeueToTail(channel, message, headersKeyRequeueToTailCounter, requeueCounter, maxRequeueTimes);

        messageItem.setStatus(StatusEnums.REQUEUE_SUCCESS);
    }

    @Override
    public String formatHeadersAsString(HyggeRabbitMqListenerContext<T> context, Map<String, Object> headers) {
        return jsonHelper.formatAsString(headers);
    }

    @Override
    public String formatBodyAsString(HyggeRabbitMqListenerContext<T> context, Message message) {
        return new String(message.getBody(), StandardCharsets.UTF_8);
    }

    @Override
    public void printMessageEntityLog(HyggeRabbitMqListenerContext<T> context, String prefixInfo) {
        HyggeRabbitMQMessageItem<T> messageItem = context.getRwaMessage();

        String logInfo = String.format("%s%sheaders:%s%sbody:%s",
                prefixInfo,
                ConstantParameters.LINE_SEPARATOR,
                messageItem.getHeadersStringVal(),
                ConstantParameters.LINE_SEPARATOR,
                messageItem.getMessageStringVal());

        printLog(context.getLoglevel(), logInfo, messageItem.getException());
    }

    @Override
    public void autoAck(HyggeRabbitMqListenerContext<T> context) {
        HyggeRabbitMQMessageItem<T> messageItem = context.getRwaMessage();
        try {
            // 自动 ack
            if (!messageItem.isAutoAckTriggered()) {
                messageItem.nackStatusCheckAndReset();

                switch (messageItem.getStatus()) {
                    case NEEDS_ACK:
                        ack(context, messageItem);
                        break;
                    case NEEDS_NACK:
                        nack(context, messageItem);
                        break;
                    default:
                        throw new InternalRuntimeException("Status should be one of NEEDS_ACK/NEEDS_NACK but we found " + messageItem.getStatus() + ".");
                }

                // 并不知晓手动 ack 逻辑，所以仅对自动 ack 异常进行输出
                if (messageItem.isExceptionOccurred()) {
                    String prefixInfo = String.format("HyggeListener(%s): Message(%s) was consumed failed, and this message was discarded.", getListenerName(), messageItem.getStatus().toString());
                    printMessageEntityLog(context, prefixInfo);
                }
            }
        } catch (Exception e) {
            // 将异常存入上下文
            messageItem.setException(e);
            context.setLoglevelIntelligently(LogLevel.ERROR);

            String prefixInfo = String.format("HyggeListener(%s): Message(%s) failed to auto ack.", getListenerName(), messageItem.getStatus().toString());
            printMessageEntityLog(context, prefixInfo);
        }
    }
}
