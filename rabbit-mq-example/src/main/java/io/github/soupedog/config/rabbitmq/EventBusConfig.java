package io.github.soupedog.config.rabbitmq;

import io.github.soupedog.config.rabbitmq.configuration.RabbitMqConfigurationProperties;
import io.github.soupedog.service.client.RabbitClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@Slf4j
@Configuration
public class EventBusConfig {
    private final RabbitMqConfigurationProperties properties;

    public EventBusConfig(RabbitMqConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean("eventBusHeadersExchange")
    public HeadersExchange eventBusHeadersExchange(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        HeadersExchange exchange = new HeadersExchange(properties.getEventBus().getExchange());
        exchange.setAdminsThatShouldDeclare(rabbitAdmin);
        return exchange;
    }

    @Bean("deadEventBusHeadersExchange")
    public HeadersExchange deadEventBusHeadersExchange(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin, @Qualifier("eventBusHeadersExchange") HeadersExchange exchange, @Autowired ConfigurableBeanFactory factory) {
        HeadersExchange topicExchange = new HeadersExchange(properties.getEventBus().getDeadExchange());
        topicExchange.setAdminsThatShouldDeclare(rabbitAdmin);

        // 动态注册一下 Events
        ArrayList<String> events = properties.getEventBus().getEventNames();

        for (String eventName : events) {
            Queue queue = QueueBuilder
                    .durable("event-" + eventName)
                    .ttl(properties.getEventBus().getTtlMillisecond())
                    .deadLetterExchange(properties.getEventBus().getDeadExchange())
                    .build();
            queue.setAdminsThatShouldDeclare(rabbitAdmin);

            HashMap<String, Object> matchMap = new HashMap<>();
            matchMap.put(RabbitClient.KEY_EVENT_TYPE, eventName);

            Binding binding = BindingBuilder.bind(queue).to(exchange)
                    .whereAll(matchMap)
                    .match();

            binding.setAdminsThatShouldDeclare(rabbitAdmin);

            // 注册到上下文
            factory.registerSingleton("event-" + eventName, queue);
            // 注册到上下文
            factory.registerSingleton("binding-event-" + eventName, binding);

            if (rabbitAdmin.getQueueInfo(queue.getName()) == null) {
                rabbitAdmin.declareQueue(queue);
                rabbitAdmin.declareBinding(binding);
                log.info("EventBus init {} success.", eventName);
            }
        }
        return topicExchange;
    }

    @Bean("deadMessageQueue")
    public Queue deadMessageQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        Queue queue = QueueBuilder
                .durable("dead-events")
                .build();
        queue.setAdminsThatShouldDeclare(rabbitAdmin);
        return queue;
    }

    @Bean
    public Binding bindingDeadMessageQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin, @Qualifier("deadMessageQueue") Queue queue, @Qualifier("eventBusHeadersExchange") HeadersExchange exchange) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).whereAny(RabbitClient.KEY_EVENT_TYPE).exist();

        binding.setAdminsThatShouldDeclare(rabbitAdmin);
        return binding;
    }
}
