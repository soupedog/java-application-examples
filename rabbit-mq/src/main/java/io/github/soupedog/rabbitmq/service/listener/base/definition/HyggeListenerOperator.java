package io.github.soupedog.rabbitmq.service.listener.base.definition;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import hygge.commons.exception.InternalRuntimeException;
import io.github.soupedog.rabbitmq.service.listener.base.StatusEnums;
import io.github.soupedog.rabbitmq.service.listener.base.HyggeRabbitMQMessageItem;
import io.github.soupedog.rabbitmq.service.listener.base.HyggeRabbitMqBatchListenerContext;
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
            headers.put(headersCounterKey, Integer.toString(requeueCounter + 1));
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
    default void ack(HyggeRabbitMqListenerContextFeature context, HyggeRabbitMQMessageItem<?> messageItem) {
        try {
            Message message = messageItem.getMessage();
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            context.getChannel().basicAck(deliveryTag, false);
            messageItem.setStatus(StatusEnums.ACK_SUCCESS);
        } catch (Exception e) {
            messageItem.setException(e);
            context.setLoglevelIntelligently(LogLevel.ERROR);
            messageItem.setStatus(StatusEnums.ACK_UN_ACKED);
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
            for (HyggeRabbitMQMessageItem<?> item : context.getRawMessageList()) {
                item.setStatus(StatusEnums.ACK_SUCCESS);
                item.setAutoAckTriggered(true);
            }
        } catch (Exception e) {
            for (HyggeRabbitMQMessageItem<?> item : context.getRawMessageList()) {
                item.setStatus(StatusEnums.ACK_UN_ACKED);
                item.setException(e);
            }
            context.setLoglevelIntelligently(LogLevel.ERROR);
        }
    }

    /**
     * 单条消息进行 nack
     */
    default void nack(HyggeRabbitMqListenerContextFeature context, HyggeRabbitMQMessageItem<?> messageItem) {
        try {
            Message message = messageItem.getMessage();
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            // 丢弃消息
            context.getChannel().basicNack(deliveryTag, false, false);
            messageItem.setStatus(StatusEnums.NACK_SUCCESS);
        } catch (Exception e) {
            messageItem.setException(e);
            messageItem.setStatus(StatusEnums.NACK_UN_ACKED);
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
            for (HyggeRabbitMQMessageItem<?> item : context.getRawMessageList()) {
                item.setStatus(StatusEnums.NACK_SUCCESS);
                item.setAutoAckTriggered(true);
            }
            context.setLoglevelIntelligently(LogLevel.ERROR);
        } catch (Exception e) {
            for (HyggeRabbitMQMessageItem<?> item : context.getRawMessageList()) {
                item.setStatus(StatusEnums.NACK_UN_ACKED);
                item.setException(e);
            }
            context.setLoglevelIntelligently(LogLevel.ERROR);
        }
    }
}
