package io.github.soupedog.kafka.config;

import io.github.soupedog.kafka.service.MyManualAckListener;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Xavier
 * @date 2026/4/14
 */
@Configuration
public class Config {
    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topic1() {
        return TopicBuilder.name("thing1")
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public ProducerFactory<Long, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // See https://kafka.apache.org/documentation/#producerconfigs for more properties
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean("MainKafkaTemplate")
    public KafkaTemplate<Long, String> kafkaTemplate() {
        return new KafkaTemplate<Long, String>(producerFactory());
    }

    @Bean("G1")
    public ConsumerFactory<Long, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "thing1-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentMessageListenerContainer<Long, String> thing1Container(@Qualifier("G1") ConsumerFactory<Long, String> consumerFactory) {
        // 1. 设置要监听的主题
        ContainerProperties containerProps = new ContainerProperties("thing1");
        // 2. 设置消息监听器，定义收到消息后的处理逻辑
        containerProps.setMessageListener(new MyManualAckListener());
        // 3. 创建并发容器
        ConcurrentMessageListenerContainer<Long, String> container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProps);
        // 4. 设置并发数（线程数），建议不超过主题的分区数，否则会有线程空闲
        container.setConcurrency(1);
        // 设置手工 ACK(自动的话是一种轮询机制，引发多次消费时间窗口会变大)
        container.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return container;
    }
}
