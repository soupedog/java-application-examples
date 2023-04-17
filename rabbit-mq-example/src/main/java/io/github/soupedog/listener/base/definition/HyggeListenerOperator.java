package io.github.soupedog.listener.base.definition;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import hygge.commons.exception.InternalRuntimeException;
import io.github.soupedog.listener.base.ActionEnum;
import io.github.soupedog.listener.base.HyggeBatchMessageItem;
import io.github.soupedog.listener.base.HyggeRabbitMqBatchListenerContext;
import io.github.soupedog.listener.base.HyggeRabbitMqListenerContext;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.boot.logging.LogLevel;

import java.io.IOException;
import java.util.Map;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
public interface HyggeListenerOperator {

    /**
     * 将消息丢回队尾
     */
    default void requeueToTail(Channel channel, Message message, String headersCounterKey, int requeueCounter, int maxRequeueTimes) throws IOException {
        MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headers = messageProperties.getHeaders();

        if (requeueCounter < maxRequeueTimes) {
            headers.put(headersCounterKey, Integer.valueOf(requeueCounter + 1).toString());
        } else {
            throw new InternalRuntimeException("Exceeds the maximum(" + maxRequeueTimes + ") number of requeue-to-tail.");
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
    }

    /**
     * 单条消息进行 ack
     */
    default void ack(HyggeRabbitMqListenerContext<Message> context) throws Exception {
        try {
            long deliveryTag = context.getRwaMessage().getMessageProperties().getDeliveryTag();
            context.getChannel().basicAck(deliveryTag, false);
            context.setAutoAckTriggered(true);
        } catch (Exception e) {
            // 负反馈机制，防止加剧 ack/nack 不正常，不许重试
            context.setRetryable(false);
            throw e;
        }
    }

    /**
     * 单条消息进行 nack
     */
    default void nack(HyggeRabbitMqListenerContext<Message> context) throws Exception {
        try {
            // 丢弃
            long deliveryTag = context.getRwaMessage().getMessageProperties().getDeliveryTag();
            context.getChannel().basicNack(deliveryTag, false, false);
            context.setAutoAckTriggered(true);
        } catch (Exception e) {
            // 负反馈机制，防止加剧 ack/nack 不正常，不许重试
            context.setRetryable(false);
            throw e;
        }
    }

    /**
     * 单条消息进行 ack
     */
    default void ack(HyggeRabbitMqBatchListenerContext<?> context, HyggeBatchMessageItem<?> messageItem) {
        try {
            Message message = messageItem.getMessage();
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            context.getChannel().basicAck(deliveryTag, false);
            messageItem.setAction(ActionEnum.ACK_SUCCESS);
        } catch (Exception e) {
            messageItem.setAction(ActionEnum.ACK_UN_ACKED);
            messageItem.setThrowable(e);
            context.setLoglevelIntelligently(LogLevel.ERROR);
        } finally {
            messageItem.setAutoAckTriggered(true);
        }
    }

    /**
     * 消息批量进行 ack
     */
    default void ackMultiple(HyggeRabbitMqBatchListenerContext<?> context) {
        try {
            context.getChannel().basicAck(context.getMaxDeliveryTag(), true);
            for (HyggeBatchMessageItem<?> item : context.getRawMessageList()) {
                item.setAction(ActionEnum.ACK_SUCCESS);
                item.setAutoAckTriggered(true);
            }
        } catch (Exception e) {
            for (HyggeBatchMessageItem<?> item : context.getRawMessageList()) {
                item.setAction(ActionEnum.ACK_UN_ACKED);
                item.setThrowable(e);
            }
            context.setLoglevelIntelligently(LogLevel.ERROR);
        }
    }

    /**
     * 单条消息进行 nack
     */
    default void nack(HyggeRabbitMqBatchListenerContext<?> context, HyggeBatchMessageItem<?> messageItem) {
        try {
            Message message = messageItem.getMessage();
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            // 丢弃消息
            context.getChannel().basicNack(deliveryTag, false, false);
            messageItem.setAction(ActionEnum.NACK_SUCCESS);
        } catch (Exception e) {
            messageItem.setAction(ActionEnum.NACK_UN_ACKED);
            messageItem.setThrowable(e);
            context.setLoglevelIntelligently(LogLevel.ERROR);
        } finally {
            messageItem.setAutoAckTriggered(true);
        }
    }

    /**
     * 消息批量进行 nack
     */
    default void nackMultiple(HyggeRabbitMqBatchListenerContext<?> context) {
        try {
            // 丢弃消息
            context.getChannel().basicNack(context.getMaxDeliveryTag(), true, false);
            for (HyggeBatchMessageItem<?> item : context.getRawMessageList()) {
                item.setAction(ActionEnum.NACK_SUCCESS);
                item.setAutoAckTriggered(true);
            }
            context.setLoglevelIntelligently(LogLevel.WARN);
        } catch (Exception e) {
            for (HyggeBatchMessageItem<?> item : context.getRawMessageList()) {
                item.setAction(ActionEnum.NACK_UN_ACKED);
                item.setThrowable(e);
            }
            context.setLoglevelIntelligently(LogLevel.ERROR);
        }
    }
}
