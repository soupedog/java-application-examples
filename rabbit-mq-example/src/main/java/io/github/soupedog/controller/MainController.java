package io.github.soupedog.controller;

import io.github.soupedog.domain.User;
import io.github.soupedog.service.client.RabbitClient;
import hygge.web.template.definition.HyggeController;
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

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@RestController
public class MainController implements HyggeController<ResponseEntity<?>> {
    @Autowired
    private RabbitClient rabbitClient;
    @Autowired
    @Qualifier("maiRabbitAdmin")
    private RabbitAdmin admin;

    @GetMapping("/queue")
    public Object test(@RequestParam(name = "queueName") String queueName) {
        QueueInformation queueInformation = admin.getQueueInfo(queueName);
        return queueInformation;
    }

    @PostMapping("/main/topic/send")
    public Object test2(@RequestParam(name = "exchange", required = false, defaultValue = "${test.demo.rabbit.main.exchange}.topic") String exchange,
                        @RequestParam(name = "routingKey", required = false, defaultValue = "${test.demo.rabbit.main.routing-key}") String routingKey,
                        @RequestParam(name = "redoTimes", required = false, defaultValue = "1") int redoTimes,
                        @RequestBody User user) {

        for (int i = 0; i < redoTimes; i++) {
            Message message = rabbitClient.buildMessage(user);

            rabbitClient.sendMessageByExchangeAndRoutingKey(message, exchange, routingKey);
        }

        return success(Timestamp.from(Instant.now()));
    }

    @PostMapping("/main/event/send")
    public Object test3(@RequestParam(name = "eventType", required = false, defaultValue = "A") String eventType,
                        @RequestParam(name = "redoTimes", required = false, defaultValue = "1") int redoTimes,
                        @RequestBody User user) {

        for (int i = 0; i < redoTimes; i++) {
            Message message = rabbitClient.buildMessage(user);
            rabbitClient.sendMessageByEvent(message, eventType);
        }

        return success(Timestamp.from(Instant.now()));
    }
}
