package io.github.soupedog.config.rabbitmq;

import io.github.soupedog.config.rabbitmq.configuration.RabbitMqConfigurationProperties;
import io.github.soupedog.service.client.RabbitMqDelayMessageRetryClient;
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
 * @date 2023/4/14
 * @since 1.0
 */
@Configuration
public class MainConfig {
    private RabbitMqDelayMessageRetryClient.Configuration DEFAULT_CONFIG = new RabbitMqDelayMessageRetryClient.Configuration();
    private final RabbitMqConfigurationProperties properties;
    private final RabbitMqDelayMessageRetryClient retryClient;

    public MainConfig(RabbitMqConfigurationProperties properties, RabbitMqDelayMessageRetryClient retryClient) {
        this.properties = properties;
        this.retryClient = retryClient;
    }

    @Bean("mainTopicExchange")
    public TopicExchange mainTopicExchange(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        TopicExchange topicExchange = new TopicExchange(properties.getMain().getExchange() + ".topic");
        // true 是默认值
        topicExchange.setShouldDeclare(true);
        topicExchange.setAdminsThatShouldDeclare(rabbitAdmin);
        return topicExchange;
    }

    @Bean("mainTopicQueue")
    public Queue mainQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        Queue queue = QueueBuilder
                .durable(properties.getMain().getExchange() + ".topic")
                .build();
        queue.setAdminsThatShouldDeclare(rabbitAdmin);
        return queue;
    }

    @Bean
    public Binding bindingMainTopicQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin, @Qualifier("mainTopicQueue") Queue queue, @Qualifier("mainTopicExchange") TopicExchange exchange) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(properties.getMain().getRoutingKey());

        binding.setAdminsThatShouldDeclare(rabbitAdmin);

        retryClient.initDelayResource(queue.getName(), exchange.getName(), binding.getRoutingKey(), DEFAULT_CONFIG);
        return binding;
    }
}
