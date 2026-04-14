package io.github.soupedog.kafka.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import hygge.commons.constant.ConstantParameters;
import hygge.util.UtilCreator;
import hygge.util.definition.JsonHelper;
import io.github.soupedog.kafka.domian.HyggeKafkaMessageEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Xavier
 * @date 2026/4/14
 */
@Slf4j
@Component
public class KafkaMessageClient {
    private final JsonHelper<ObjectMapper> jsonHelper = UtilCreator.INSTANCE.getDefaultJsonHelperInstance(false);
    private final KafkaTemplate<Long, String> kafkaTemplate;

    public KafkaMessageClient(@Qualifier("MainKafkaTemplate") KafkaTemplate<Long, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, Long key, HyggeKafkaMessageEntity entity) {
        String messageStringVal = jsonHelper.formatAsString(entity.getMessageEntity());
        ProducerRecord<Long, String> producerRecord = buildProducerRecord(topic, key, messageStringVal);
        kafkaTemplate.send(producerRecord);
        String logInfo = String.format("KafkaMessageClient send message. topic:%s key:%s%sbody:%s",
                topic,
                key,
                ConstantParameters.LINE_SEPARATOR,
                messageStringVal);
        log.info(logInfo);
    }

    public <K, V> ProducerRecord<K, V> buildProducerRecord(String topic, K key, V entity) {
        return new ProducerRecord<>(topic, key, entity);
    }
}
