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
 * @date 2023/4/14
 * @since 1.0
 */
@Configuration
public class MainConfig {
    private final RabbitMqConfigurationProperties properties;

    public MainConfig(RabbitMqConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean("mainTopicExchange")
    public TopicExchange mainTopicExchange(@Qualifier("maiRabbitAdmin") RabbitAdmin rabbitAdmin) {
        TopicExchange topicExchange = new TopicExchange(properties.getMain().getExchange() + ".topic");
        // true 是默认值
        topicExchange.setShouldDeclare(true);
        topicExchange.setAdminsThatShouldDeclare(rabbitAdmin);
        return topicExchange;
    }

    @Bean("mainTopicQueue")
    public Queue mainQueue(@Qualifier("maiRabbitAdmin") RabbitAdmin rabbitAdmin) {
        Queue queue = QueueBuilder
                .durable(properties.getMain().getExchange() + ".topic")
                .build();
        queue.setAdminsThatShouldDeclare(rabbitAdmin);
        return queue;
    }

    @Bean
    public Binding bindingMainTopicQueue(@Qualifier("maiRabbitAdmin") RabbitAdmin rabbitAdmin, @Qualifier("mainTopicQueue") Queue queue, @Qualifier("mainTopicExchange") TopicExchange exchange) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(properties.getMain().getRoutingKey());

        binding.setAdminsThatShouldDeclare(rabbitAdmin);
        return binding;
    }

}
