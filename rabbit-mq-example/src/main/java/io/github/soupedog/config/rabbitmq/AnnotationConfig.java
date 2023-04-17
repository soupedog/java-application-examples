package io.github.soupedog.config.rabbitmq;

import io.github.soupedog.config.rabbitmq.configuration.RabbitMqConfigurationProperties;
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
        TopicExchange topicExchange = new TopicExchange(properties.getAnnotation().getExchange() + ".topic");
        topicExchange.setAdminsThatShouldDeclare(rabbitAdmin);
        return topicExchange;
    }

    @Bean("annotationTopicQueue")
    public Queue annotationTopicQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        Queue queue = QueueBuilder
                .durable(properties.getAnnotation().getExchange() + ".topic")
                .ttl(properties.getAnnotation().getTtlMillisecond())
                // 配置为死信形成循环
                .deadLetterExchange(properties.getAnnotation().getDeadExchange() + ".topic")
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
        TopicExchange topicExchange = new TopicExchange(properties.getAnnotation().getDeadExchange() + ".topic");
        topicExchange.setAdminsThatShouldDeclare(rabbitAdmin);
        return topicExchange;
    }

    @Bean("deadAnnotationTopicQueue")
    public Queue deadAnnotationTopicQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        Queue queue = QueueBuilder
                .durable(properties.getAnnotation().getDeadExchange() + ".topic")
                .ttl(properties.getAnnotation().getDeadTtlMillisecond())
                // 配置为正常形成循环
                .deadLetterExchange(properties.getAnnotation().getExchange() + ".topic")
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
