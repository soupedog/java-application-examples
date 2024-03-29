package io.github.soupedog.rabbitmq.config;

import io.github.soupedog.rabbitmq.config.configuration.RabbitMqConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Configuration
public class AnnotationConfig {
    private final RabbitMqConfigurationProperties properties;

    public AnnotationConfig(RabbitMqConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean("annotationTopicExchange")
    public TopicExchange annotationTopicExchange(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        TopicExchange topicExchange = new TopicExchange(properties.getAnnotation().getExchange());
        topicExchange.setAdminsThatShouldDeclare(rabbitAdmin);
        return topicExchange;
    }

    @Bean("annotationTopicQueue")
    public Queue annotationTopicQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        Queue queue = QueueBuilder
                .durable(properties.getAnnotation().getExchange())
                .ttl(properties.getAnnotation().getTtlMillisecond())
                // 配置为死信形成循环
                .deadLetterExchange(properties.getAnnotation().getDeadExchange())
                .deadLetterRoutingKey(properties.getAnnotation().getDeadRoutingKey())
                .build();
        queue.setAdminsThatShouldDeclare(rabbitAdmin);
        return queue;
    }

    @Bean
    public Binding bindingAnnotationTopicQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin, @Qualifier("annotationTopicQueue") Queue queue, @Qualifier("annotationTopicExchange") TopicExchange exchange) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(properties.getAnnotation().getRoutingKey());

        binding.setAdminsThatShouldDeclare(rabbitAdmin);
        return binding;
    }

    @Bean("deadAnnotationTopicExchange")
    public TopicExchange deadAnnotationTopicExchange(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        TopicExchange topicExchange = new TopicExchange(properties.getAnnotation().getDeadExchange());
        topicExchange.setAdminsThatShouldDeclare(rabbitAdmin);
        return topicExchange;
    }

    @Bean("deadAnnotationTopicQueue")
    public Queue deadAnnotationTopicQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        Queue queue = QueueBuilder
                .durable(properties.getAnnotation().getDeadExchange() )
                .ttl(properties.getAnnotation().getDeadTtlMillisecond())
                // 配置为正常形成循环
                .deadLetterExchange(properties.getAnnotation().getExchange())
                .deadLetterRoutingKey(properties.getAnnotation().getRoutingKey())
                .build();
        queue.setAdminsThatShouldDeclare(rabbitAdmin);
        return queue;
    }

    @Bean
    public Binding bindingDeadAnnotationTopicQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin, @Qualifier("deadAnnotationTopicQueue") Queue queue, @Qualifier("deadAnnotationTopicExchange") TopicExchange exchange) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(properties.getAnnotation().getDeadRoutingKey());

        binding.setAdminsThatShouldDeclare(rabbitAdmin);
        return binding;
    }
}
