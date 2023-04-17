package io.github.soupedog.service.client;

import io.github.soupedog.config.rabbitmq.configuration.RabbitMqConfigurationProperties;
import io.github.soupedog.listener.base.definition.HyggeRabbitMessageEntity;
import hygge.commons.constant.ConstantParameters;
import hygge.web.template.HyggeWebUtilContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@Slf4j
@Component
public class RabbitClient extends HyggeWebUtilContainer {
    public static final String HEADERS_KEY_HYGGE_UNIQUE_ID = "hygge-unique-id";
    public static final String KEY_EVENT_TYPE = "event-type";

    @Autowired
    @Qualifier("mainRabbitTemplate")
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitMqConfigurationProperties properties;

    public void sendMessageByEvent(Message message, String eventType) {
        message.getMessageProperties().setHeader(KEY_EVENT_TYPE, eventType);

        CorrelationData correlationData = new CorrelationData();

        rabbitTemplate.send(properties.getEventBus().getExchange(), "", message, correlationData);

        String messageStringVal = new String(message.getBody(), StandardCharsets.UTF_8);
        String headersStringVal = jsonHelper.formatAsString(message.getMessageProperties().getHeaders());

        log.info("Triggered event {}. headers:{} body:{}", eventType, headersStringVal, messageStringVal);
    }

    public void sendMessageByExchangeAndRoutingKey(Message message, String exchange, String routingKey) {
        String messageStringVal = new String(message.getBody(), StandardCharsets.UTF_8);
        String headersStringVal = jsonHelper.formatAsString(message.getMessageProperties().getHeaders());

        rabbitTemplate.send(exchange, routingKey, message);

        String logInfo = String.format("RabbitClient send message. exchange:%s routingKey:%s%sheaders:%s%sbody:%s",
                exchange,
                routingKey,
                ConstantParameters.LINE_SEPARATOR,
                headersStringVal,
                ConstantParameters.LINE_SEPARATOR,
                messageStringVal);
//        log.info(logInfo);
    }

    public Message buildMessage(HyggeRabbitMessageEntity entity) {
        String body = jsonHelper.formatAsString(entity);
        return MessageBuilder.withBody(body.getBytes(StandardCharsets.UTF_8))
                .setContentType("application/json")
                .setHeader(HEADERS_KEY_HYGGE_UNIQUE_ID, entity.getUniqueIdentification())
                .build();
    }
}
