package io.github.soupedog.rabbitmq.config;

import io.github.soupedog.rabbitmq.service.client.RabbitMqDelayMessageRetryClient;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Xavier
 * @date 2023/4/18
 * @since 1.0
 */
@Configuration
public class RetryConfig {
    @Bean
    public RabbitMqDelayMessageRetryClient rabbitMqDelayMessageRetryClient(@Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin) {
        RabbitMqDelayMessageRetryClient retryClient = new RabbitMqDelayMessageRetryClient(rabbitAdmin);
        retryClient.setStaticRetryExchangeName("hygge-delay-retry");
        return retryClient;
    }
}
