package io.github.soupedog.config.rabbitmq.utils;

import hygge.web.template.HyggeWebUtilContainer;
import org.slf4j.Logger;
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
    private RabbitmqTemplateBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static RabbitTemplate buildRabbitTemplate(ConnectionFactory connectionFactory, Logger logger, String modelName) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 开启回调处理
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setBeforePublishPostProcessors();
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
            return target;
        }
    }

}
