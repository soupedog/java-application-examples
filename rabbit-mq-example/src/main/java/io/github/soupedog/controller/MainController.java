package io.github.soupedog.controller;

import hygge.commons.constant.enums.StringCategoryEnum;
import hygge.web.template.HyggeWebUtilContainer;
import hygge.web.template.definition.HyggeController;
import io.github.soupedog.domain.User;
import io.github.soupedog.service.client.RabbitClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;

import static io.github.soupedog.listener.base.definition.HyggeListenerBaseFeature.DEFAULT_HEADERS_KEY_ENVIRONMENT_NAME;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@RestController
public class MainController extends HyggeWebUtilContainer implements HyggeController<ResponseEntity<?>> {
    @Autowired
    private RabbitClient rabbitClient;
    @Autowired
    @Qualifier("mainRabbitAdmin")
    private RabbitAdmin admin;

    @GetMapping("/queue")
    public Object queueInfo(@RequestParam(name = "queueName") String queueName) {
        QueueInformation queueInformation = admin.getQueueInfo(queueName);
        return queueInformation;
    }

    @PostMapping("/main/topic/send")
    public Object main(@RequestParam(name = "exchange", required = false, defaultValue = "${test.demo.rabbit.main.exchange}.topic") String exchange,
                       @RequestParam(name = "routingKey", required = false, defaultValue = "${test.demo.rabbit.main.routing-key}") String routingKey,
                       @RequestParam(name = "redoTimes", required = false, defaultValue = "1") int redoTimes,
                       @RequestBody User user) {

        for (int i = 0; i < redoTimes; i++) {
            Message message = rabbitClient.buildMessage(user);

            rabbitClient.sendMessageByExchangeAndRoutingKey(message, exchange, routingKey);
        }

        return success(Timestamp.from(Instant.now()));
    }

    @PostMapping("/batch/topic/send")
    public Object batch(@RequestParam(name = "exchange", required = false, defaultValue = "${test.demo.rabbit.batch.exchange}.topic") String exchange,
                        @RequestParam(name = "routingKey", required = false, defaultValue = "${test.demo.rabbit.batch.routing-key}") String routingKey,
                        @RequestParam(name = "redoTimes", required = false, defaultValue = "10") int redoTimes,
                        @RequestBody User user) {

        for (int i = 0; i < redoTimes; i++) {
            User current = user.deepClone();

            current.setUid(user.getUid() + "--" + i);
            current.setName(randomHelper.getRandomString(6, StringCategoryEnum.A_Z, StringCategoryEnum.a_z, StringCategoryEnum.NUMBER));
            current.setAge(randomHelper.getRandomInteger(0, 120));

            Message message = rabbitClient.buildMessage(current);

            if (randomHelper.getRandomInteger(0, 9) > 8) {
                message.getMessageProperties().getHeaders().put(DEFAULT_HEADERS_KEY_ENVIRONMENT_NAME, "sit");
            }
            rabbitClient.sendMessageByExchangeAndRoutingKey(message, exchange, routingKey);
        }

        return success(Timestamp.from(Instant.now()));
    }

    @PostMapping("/main/event/send")
    public Object event(@RequestParam(name = "eventType", required = false, defaultValue = "A") String eventType,
                        @RequestParam(name = "redoTimes", required = false, defaultValue = "1") int redoTimes,
                        @RequestBody User user) {

        for (int i = 0; i < redoTimes; i++) {
            Message message = rabbitClient.buildMessage(user);
            rabbitClient.sendMessageByEvent(message, eventType);
        }

        return success(Timestamp.from(Instant.now()));
    }
}
