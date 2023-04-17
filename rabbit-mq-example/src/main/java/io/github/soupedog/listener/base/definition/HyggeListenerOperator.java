package io.github.soupedog.listener.base.definition;

import io.github.soupedog.listener.base.ActionEnum;
import io.github.soupedog.listener.base.HyggeBatchMessageItem;
import io.github.soupedog.listener.base.HyggeRabbitMqBatchListenerContext;
import io.github.soupedog.listener.base.HyggeRabbitMqListenerContext;
import org.springframework.amqp.core.Message;
import org.springframework.boot.logging.LogLevel;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
public interface HyggeListenerOperator {

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
     * 单条消息进行 nack
     */
    default void nack(HyggeRabbitMqBatchListenerContext<?> context, HyggeBatchMessageItem<?> messageItem) {
        try {
            Message message = messageItem.getMessage();
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            // 丢弃消息
            context.getChannel().basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            messageItem.setAction(ActionEnum.NACK_UN_ACKED);
            messageItem.setThrowable(e);
            context.setLoglevelIntelligently(LogLevel.ERROR);
        } finally {
            messageItem.setAutoAckTriggered(true);
        }
    }
}
