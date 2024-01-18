package io.github.soupedog.rabbitmq.config;

import io.github.soupedog.rabbitmq.config.configuration.RabbitMqConfigurationProperties;
import io.github.soupedog.rabbitmq.service.client.RabbitClient;
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

    /**
     * 这个队列没有消费者
     */
    @Bean("deadMessageQueue")
    public Queue deadMessageQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        Queue queue = QueueBuilder
                .durable("dead-events")
                .build();
        queue.setAdminsThatShouldDeclare(rabbitAdmin);
        return queue;
    }

    /**
     * 匹配任何 Headers 中有 event 类型的消息(不存在独占唯一，event-A 会匹配到其他队列，也会冗余投递到此处 )
     */
    @Bean
    public Binding bindingDeadMessageQueue(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin, @Qualifier("deadMessageQueue") Queue queue, @Qualifier("eventBusHeadersExchange") HeadersExchange exchange) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).whereAny(RabbitClient.KEY_EVENT_TYPE).exist();

        binding.setAdminsThatShouldDeclare(rabbitAdmin);
        return binding;
    }
}
