package io.github.soupedog.listener.base;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import hygge.commons.constant.ConstantParameters;
import hygge.commons.exception.InternalRuntimeException;
import hygge.commons.exception.ParameterRuntimeException;
import hygge.util.UtilCreator;
import hygge.util.definition.JsonHelper;
import hygge.util.definition.ParameterHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public abstract class HyggeChannelAwareMessageListener<T> implements HyggeListenerFeature<T>, ChannelAwareMessageListener {
    protected String listenerName;
    protected String environmentName;
    private static final Logger log = LoggerFactory.getLogger(HyggeChannelAwareMessageListener.class);
    protected static final JsonHelper<?> jsonHelper = UtilCreator.INSTANCE.getDefaultJsonHelperInstance(false);
    protected static final ParameterHelper parameterHelper = UtilCreator.INSTANCE.getDefaultInstance(ParameterHelper.class);
    protected long requeueToTailMillisecondInterval = 500L;
    protected int maxRequeueTimes = 1000;

    protected static final String KEY_ENVIRONMENT_NAME = "environmentName";
    protected static final String KEY_REQUEUE_COUNTER = "requeueCounter";

    public HyggeChannelAwareMessageListener(String listenerName, String environmentName) {
        this.listenerName = listenerName;
        this.environmentName = environmentName;
    }

    @Override
    public void onMessage(Message message, Channel channel) {
        HyggeRabbitMqListenerContext context = new HyggeRabbitMqListenerContext();
        context.setMessage(message);
        context.setChannel(channel);

        String headersStringVal = null;
        String messageStringVal = null;

        try {
            // 是否忽略当前消息，并丢回队列尾部
            if (isRequeueToTailEnable(context)) {
                requeue(context);
                return;
            }
        } catch (Exception e) {
            headersStringVal = formatMessageHeadersAsString(context);
            messageStringVal = formatMessageBodyAsString(context);

            String logInfo = String.format("HyggeListener(%s) fail to requeue, and this message turns into a unAcked message.%sheaders:%s%sbody:%s",
                    listenerName,
                    ConstantParameters.LINE_SEPARATOR,
                    headersStringVal,
                    ConstantParameters.LINE_SEPARATOR,
                    messageStringVal);
            log.error(logInfo, e);
            // 将异常存入上下文
            context.setThrowable(e);
            return;
        }

        try {
            headersStringVal = formatMessageHeadersAsString(context);
            messageStringVal = formatMessageBodyAsString(context);

            T messageEntity = formatMessageAsEntity(context, messageStringVal);

            printMessageLog(context, headersStringVal, messageStringVal);

            onReceive(context, messageEntity);
        } catch (Exception e) {
            // 将异常存入上下文
            context.setThrowable(e);
            context.setRetryable(false);
        } finally {
            // 上下文中存在异常的统一会在此处处理
            autoAck(context, headersStringVal, messageStringVal);

            // 如果未抛出异常且未进行重试，则激活 finishHook 环节
            try {
                if (context.isNoExceptionOccurred()) {
                    context.setFinishHookEnable(!retryHook(context));
                }
            } catch (Exception e) {
                String logInfo = String.format("HyggeListener(%s) fail to execute retryHook, and the finishHook method is automatically disabled.", listenerName);
                log.error(logInfo, e);
                // 将异常存入上下文
                context.setThrowable(e);
            }

            if (context.isFinishHookEnable()) {
                try {
                    finishHook(context);
                } catch (Exception e) {
                    String logInfo = String.format("HyggeListener(%s) fail to execute finishHook.", listenerName);
                    log.error(logInfo, e);
                    // 将异常存入上下文
                    context.setThrowable(e);
                }
            }
        }
    }

    @Override
    public boolean isRequeueToTailEnable(HyggeRabbitMqListenerContext context) {
        String messageEnvironmentName = getValueFromHeaders(context, KEY_ENVIRONMENT_NAME, true);

        return !environmentName.equals(messageEnvironmentName) && parameterHelper.isNotEmpty(messageEnvironmentName);
    }

    @Override
    public String formatMessageHeadersAsString(HyggeRabbitMqListenerContext context) {
        return jsonHelper.formatAsString(context.getMessage().getMessageProperties().getHeaders());
    }

    @Override
    public String formatMessageBodyAsString(HyggeRabbitMqListenerContext context) {
        return new String(context.getMessage().getBody(), StandardCharsets.UTF_8);
    }

    @Override
    public void printMessageLog(HyggeRabbitMqListenerContext context, String headersStringVal, String messageStringVal) {
        String logInfo = String.format("HyggeListener(%s) received message.%sheaders:%s%sbody:%s",
                listenerName,
                ConstantParameters.LINE_SEPARATOR,
                headersStringVal,
                ConstantParameters.LINE_SEPARATOR,
                messageStringVal);

        switch (context.getLoglevel()) {
            case TRACE:
                log.trace(logInfo);
                break;
            case DEBUG:
                log.debug(logInfo);
                break;
            case INFO:
                log.info(logInfo);
                break;
            case WARN:
                log.warn(logInfo);
                break;
            default:
                log.error(logInfo);
                break;
        }
    }

    @Override
    public void finishHook(HyggeRabbitMqListenerContext context) {
        // 默认啥也不干
    }

    /**
     * 根据 {@link HyggeRabbitMqListenerContext#isAutoAckTriggered()} 属性，如果为 true 则自动进行 ACK 操作：成功消费、消费失败丢弃
     */
    private void autoAck(HyggeRabbitMqListenerContext context, String headersStringVal, String messageStringVal) {
        try {
            // 自动 ack
            if (!context.isAutoAckTriggered()) {
                if (context.isNoExceptionOccurred()) {
                    ackSuccess(context);
                } else {
                    ackFail(context);

                    String logInfo = String.format("HyggeListener(%s) fail to consume, and this message was discarded.%sheaders:%s%sbody:%s",
                            listenerName,
                            ConstantParameters.LINE_SEPARATOR,
                            headersStringVal,
                            ConstantParameters.LINE_SEPARATOR,
                            messageStringVal);

                    log.error(logInfo, context.getThrowable());
                }
            }
        } catch (Exception e) {
            String ackInfo = context.isNoExceptionOccurred() ? "ack" : "nack";
            String logInfo = String.format("HyggeListener(%s) fail to auto %s, and this message turns into a unAcked message.%sheaders:%s%sbody:%s",
                    listenerName,
                    ackInfo,
                    ConstantParameters.LINE_SEPARATOR,
                    headersStringVal,
                    ConstantParameters.LINE_SEPARATOR,
                    messageStringVal);
            log.error(logInfo, e);

            // 将异常存入上下文(出现了异常覆盖)
            context.setThrowable(e);
        }
    }

    /**
     * 先 ack 再丢回队尾，该方法不会重复消费，但可能丢失消息
     * <p>
     * 回退异常日志样例
     * <pre>
     *      HyggeListener(HyggeTest) fail to requeue.
     *      headers:……
     *      body:……
     *      ……
     *      Caused by: hygge.commons.exception.InternalRuntimeException: Exceeds the maximum(1000) number of requeue.
     *  </pre>
     */
    protected void requeue(HyggeRabbitMqListenerContext context) {
        try {
            ackSuccess(context);

            if (context.isExceptionOccurred()) {
                // 出现异常时不进行丢回队尾行为
                return;
            }

            // 当前消息重新发送到队尾
            Channel channel = context.getChannel();
            Message message = context.getMessage();
            MessageProperties messageProperties = message.getMessageProperties();

            int requeueCounter = parameterHelper.integerFormatOfNullable(KEY_REQUEUE_COUNTER, getValueFromHeaders(context, KEY_REQUEUE_COUNTER, true), 0);

            Map<String, Object> headers = messageProperties.getHeaders();

            if (requeueCounter < maxRequeueTimes) {
                headers.put(KEY_REQUEUE_COUNTER, Integer.valueOf(requeueCounter + 1).toString());
            } else {
                throw new InternalRuntimeException("Exceeds the maximum(" + maxRequeueTimes + ") number of requeue.");
            }

            AMQP.BasicProperties basicProperties = new AMQP.BasicProperties(messageProperties.getContentType(),
                    messageProperties.getContentEncoding(),
                    headers,
                    messageProperties.getReceivedDeliveryMode() == null ? MessageDeliveryMode.toInt(MessageDeliveryMode.PERSISTENT) : MessageDeliveryMode.toInt(messageProperties.getReceivedDeliveryMode()),
                    messageProperties.getPriority(),
                    messageProperties.getCorrelationId(),
                    messageProperties.getReplyTo(),
                    messageProperties.getExpiration(),
                    messageProperties.getMessageId(),
                    messageProperties.getTimestamp(),
                    messageProperties.getType(),
                    messageProperties.getUserId(),
                    messageProperties.getAppId(),
                    messageProperties.getClusterId());

            channel.basicPublish(message.getMessageProperties().getReceivedExchange(),
                    message.getMessageProperties().getReceivedRoutingKey(),
                    basicProperties,
                    message.getBody());

            // 防止重新投递过于迅速
            Thread.sleep(requeueToTailMillisecondInterval);
        } catch (Exception e) {
            // 负反馈机制，防止加剧 ack/nack 不正常，不许重试
            context.setRetryable(false);

            String logInfo = String.format("HyggeListener(%s) fail to requeue.", listenerName);
            throw new InternalRuntimeException(logInfo, e);
        }
    }

    protected void ackSuccess(HyggeRabbitMqListenerContext context) {
        try {
            long deliveryTag = context.getMessage().getMessageProperties().getDeliveryTag();
            context.getChannel().basicAck(deliveryTag, false);
            context.setAutoAckTriggered(true);
        } catch (IOException e) {
            // 负反馈机制，防止加剧 ack/nack 不正常，不许重试
            context.setRetryable(false);

            String logInfo = String.format("HyggeListener(%s) fail to ack.", listenerName);
            throw new InternalRuntimeException(logInfo, e);
        }
    }

    protected void ackFail(HyggeRabbitMqListenerContext context) {
        try {
            // 丢弃
            long deliveryTag = context.getMessage().getMessageProperties().getDeliveryTag();
            context.getChannel().basicNack(deliveryTag, false, false);
            context.setAutoAckTriggered(true);
        } catch (Exception e) {
            // 负反馈机制，防止加剧 ack/nack 不正常，不许重试
            context.setRetryable(false);

            String logInfo = String.format("HyggeListener(%s) fail to nack.", listenerName);
            throw new InternalRuntimeException(logInfo, e);
        }
    }

    protected String getValueFromHeaders(HyggeRabbitMqListenerContext context, String key, boolean nullable) {
        String result = getValueFromHeaders(key, context);
        if (!nullable && !StringUtils.hasText(result)) {
            // 参数有误，无法自愈，所以设置不再允许重试
            context.setRetryable(false);
            throw new ParameterRuntimeException(listenerName + " fail to get [" + key + "] from headers of rabbitmq message,it can't be empty.");
        }
        return result;
    }

    private String getValueFromHeaders(String key, HyggeRabbitMqListenerContext context) {
        return Optional.ofNullable(context)
                .map(HyggeRabbitMqListenerContext::getMessage)
                .map(Message::getMessageProperties)
                .map(messageProperties -> (String) messageProperties.getHeader(key))
                .orElse(null);
    }
}
