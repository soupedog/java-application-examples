package io.github.soupedog.rabbitmq.config.consumer;

import com.rabbitmq.client.Channel;
import hygge.commons.constant.ConstantParameters;
import hygge.util.template.HyggeJsonUtilContainer;
import io.github.soupedog.rabbitmq.config.AnnotationConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 注解也能定义 exchange binding queue，但是这种定义复用对象是个问题，就不演示了
 *
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@Slf4j
@Component
public class AnnotationConsumerConfig extends HyggeJsonUtilContainer {

    /**
     * {@link AnnotationConfig#annotationTopicQueue(RabbitAdmin)}
     */
    @RabbitListener(queues = {"${test.demo.rabbit.annotation.exchange}"})
    public void annotationConsumer(Message message, Channel channel) {
        printMessage("normal", message);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }

        // 等效于 nack requeue=false
        throw new AmqpRejectAndDontRequeueException("再投胎");
    }

    /**
     * {@link AnnotationConfig#deadAnnotationTopicQueue(RabbitAdmin)}
     */
    @RabbitListener(queues = {"${test.demo.rabbit.annotation.dead-exchange}"})
    public void deadAnnotationConsumer(Message message, Channel channel) {
        printMessage("dead", message);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }

        // 等效于 nack requeue=false
        throw new AmqpRejectAndDontRequeueException("再投胎");


        // 默认是自动处理 ack 模式，此处手动 ack 会二次 ack 抛出异常
        // channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
    }

    private static void printMessage(String logSource, Message message) {
        String headersStringVal = jsonHelper.formatAsString(message.getMessageProperties().getHeaders());
        String xDeathHeadersStringVal = jsonHelper.formatAsString(message.getMessageProperties().getXDeathHeader());
        String messageStringVal = new String(message.getBody(), StandardCharsets.UTF_8);

        String logInfo = String.format("AnnotationConsumer(%s) received message.%sheaders:%s%sxDeathHeaders:%s%sbody:%s",
                logSource,
                ConstantParameters.LINE_SEPARATOR,
                headersStringVal,
                ConstantParameters.LINE_SEPARATOR,
                xDeathHeadersStringVal,
                ConstantParameters.LINE_SEPARATOR,
                messageStringVal);

        log.info(logInfo);
    }

}
