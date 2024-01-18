package io.github.soupedog.rabbitmq.utils;

import hygge.web.template.HyggeWebUtilContainer;
import io.github.soupedog.rabbitmq.domain.MQLogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public class RabbitmqTemplateBuilder extends HyggeWebUtilContainer {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqTemplateBuilder.class);

    private RabbitmqTemplateBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static RabbitTemplate buildRabbitTemplate(ConnectionFactory connectionFactory, Logger logger, String modelName) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 开启回调处理
        rabbitTemplate.setMandatory(true);
        // 指定投递消息前要执行的某种逻辑的钩子函数(此处示例为没有钩子函数)
        rabbitTemplate.setBeforePublishPostProcessors();

        // 设置消息投递到 Exchange 的回调函数(ConnectionFactory 中为 ConfirmType.NONE 时将失效)
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {});

        // 消息未能投递到某个 Queue 时的回调函数
        rabbitTemplate.setReturnsCallback(returned -> {
            MQLogInfo mqLogInfo = new MQLogInfo(modelName);
            MQLogInfo.MessageInfo messageInfo = new MQLogInfo.MessageInfo();
            String message = new String(returned.getMessage().getBody(), StandardCharsets.UTF_8);
            messageInfo.setHeaders(returned.getMessage().getMessageProperties().getHeaders());

            messageInfo.setBody(stringAsObject(message));
            mqLogInfo.setMessage(messageInfo);

            mqLogInfo.setReplyCode(returned.getReplyCode());
            mqLogInfo.setReplyText(returned.getReplyText());
            mqLogInfo.setExchange(returned.getExchange());
            mqLogInfo.setRoutingKey(returned.getRoutingKey());

            String logMessage = jsonHelper.formatAsString(mqLogInfo);

            logger.error("RabbitTemplate({}) fail to delivery: {}",
                    modelName,
                    logMessage
            );
        });
        return rabbitTemplate;
    }

    private static Object stringAsObject(String target) {
        if (target == null) {
            return null;
        }

        if (target.isEmpty()) {
            return target;
        }

        try {
            String firstVal = target.substring(0, 1);
            switch (firstVal) {
                case "[":
                    return jsonHelper.readAsObject(target, List.class);
                case "{":
                    return jsonHelper.readAsObject(target, LinkedHashMap.class);
                default:
                    return target;
            }
        } catch (Exception e) {
            // 此处异常是 com.fasterxml.jackson.core.JsonParseException 异常的多层包装，已自带原文信息，故此处不再附加 target 原文
            log.warn("Fal to format string to json, so leave the string as is as the return value.(Although this exception does not interrupt your business logic, please fix it in time!)", e);
            return target;
        }
    }
}
