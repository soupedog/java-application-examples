package io.github.soupedog.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hygge.util.UtilCreator;
import hygge.util.definition.JsonHelper;
import io.github.soupedog.kafka.domian.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author Xavier
 * @date 2026/4/14
 */
@Slf4j
public class MyManualAckListener implements AcknowledgingMessageListener<Integer, String> {
    private JsonHelper<ObjectMapper> jsonHelper = UtilCreator.INSTANCE.getDefaultJsonHelperInstance(false);

    @Override
    public void onMessage(ConsumerRecord<Integer, String> data, Acknowledgment acknowledgment) {
        try {
            User user = jsonHelper.readAsObject(data.value(), User.class);

            // 1. 业务逻辑处理
            log.info("Received: {} {}", user.getName(), data.value());
            // 2. 业务处理成功后，手动确认
            acknowledgment.acknowledge();
        } catch (Exception e) {
            // 3. 处理失败，记录日志，不确认，消息会稍后重试
            log.error("Processing failed: {}", e.getMessage());
            // 如果需要，可以进行重试或发往死信队列等操作
        }
    }
}