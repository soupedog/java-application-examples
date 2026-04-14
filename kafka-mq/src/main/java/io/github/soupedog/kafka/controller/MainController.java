package io.github.soupedog.kafka.controller;

import hygge.util.template.HyggeJsonUtilContainer;
import hygge.web.template.definition.HyggeController;
import io.github.soupedog.kafka.domian.User;
import io.github.soupedog.kafka.service.client.KafkaMessageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Xavier
 * @date 2026/4/14
 */
@Slf4j
@RestController
public class MainController extends HyggeJsonUtilContainer implements HyggeController<ResponseEntity<?>> {
    private final KafkaMessageClient kafkaMessageClient;

    public MainController(KafkaMessageClient kafkaMessageClient) {
        this.kafkaMessageClient = kafkaMessageClient;
    }

    @PostMapping("/exchange/test/main")
    public Object exchangeTest(@RequestParam(name = "redoTimes", required = false, defaultValue = "1") int redoTimes,
                               @RequestBody User user) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (long i = 0; i < redoTimes; i++) {
            kafkaMessageClient.sendMessage("thing1", i, user);
        }
        stopWatch.stop();
        return success("耗时 (s) ：" + stopWatch.getTotalTimeSeconds());
    }
}
