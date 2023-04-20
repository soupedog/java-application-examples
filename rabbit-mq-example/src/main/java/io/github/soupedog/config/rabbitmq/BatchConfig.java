package io.github.soupedog.config.rabbitmq;

import io.github.soupedog.config.rabbitmq.configuration.RabbitMqConfigurationProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
@Configuration
public class BatchConfig {
    private final RabbitMqConfigurationProperties properties;

    public BatchConfig(RabbitMqConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean("batchTopicExchange")
    public TopicExchange batchTopicExchange(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        TopicExchange topicExchange = new TopicExchange(properties.getBatch().getExchange());
        // true 是默认值
        topicExchange.setShouldDeclare(true);
        topicExchange.setAdminsThatShouldDeclare(rabbitAdmin);
        return topicExchange;
    }

    @Bean("batchTopicQueue")
    public Queue batchQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        Queue queue = QueueBuilder
                .durable(properties.getBatch().getExchange())
                .build();
        queue.setAdminsThatShouldDeclare(rabbitAdmin);
        return queue;
    }

    @Bean
    public Binding bindingBatchTopicQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin, @Qualifier("batchTopicQueue") Queue queue, @Qualifier("batchTopicExchange") TopicExchange exchange) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(properties.getBatch().getRoutingKey());

        binding.setAdminsThatShouldDeclare(rabbitAdmin);
        return binding;
    }
}
