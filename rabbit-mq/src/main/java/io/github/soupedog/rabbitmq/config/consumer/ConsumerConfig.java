package io.github.soupedog.rabbitmq.config.consumer;

import io.github.soupedog.rabbitmq.config.EventBusConfig;
import io.github.soupedog.rabbitmq.config.configuration.RabbitMqConfigurationProperties;
import io.github.soupedog.rabbitmq.service.listener.HyggeListenerBatch;
import io.github.soupedog.rabbitmq.service.listener.HyggeEventAListener;
import io.github.soupedog.rabbitmq.service.listener.HyggeEventBListener;
import io.github.soupedog.rabbitmq.service.listener.HyggeEventCListener;
import io.github.soupedog.rabbitmq.service.listener.HyggeMainListener;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@Configuration
public class ConsumerConfig {
    private final RabbitMqConfigurationProperties properties;

    public ConsumerConfig(RabbitMqConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean("mainTopicMessageListenerContainer")
    public SimpleMessageListenerContainer mainTopicMessageListenerContainer(@Qualifier("mainRabbitmqConnectionFactory") CachingConnectionFactory mainRabbitmqConnectionFactory,
                                                                            @Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin,
                                                                            @Autowired HyggeMainListener listener,
                                                                            @Qualifier("mainTopicQueue") Queue queue) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(mainRabbitmqConnectionFactory);
        container.setAmqpAdmin(rabbitAdmin);
        container.setQueues(queue);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(listener);
        container.setPrefetchCount(1);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(1);
        return container;
    }

    @Bean("batchTopicMessageListenerContainer")
    public SimpleMessageListenerContainer batchTopicMessageListenerContainer(@Qualifier("mainRabbitmqConnectionFactory") CachingConnectionFactory mainRabbitmqConnectionFactory,
                                                                             @Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin,
                                                                             @Autowired HyggeListenerBatch listener,
                                                                             @Qualifier("batchTopicQueue") Queue queue) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(mainRabbitmqConnectionFactory);
        container.setAmqpAdmin(rabbitAdmin);
        container.setQueues(queue);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(listener);
        container.setConsumerBatchEnabled(true);
        container.setBatchSize(properties.getBatch().getBatchSize());
        container.setPrefetchCount(properties.getBatch().getBatchSize());
        container.setConcurrentConsumers(2);
        container.setMaxConcurrentConsumers(2);
        return container;
    }

    /**
     * Queue 创建过程见 {@link EventBusConfig#deadEventBusHeadersExchange(RabbitAdmin, HeadersExchange, ConfigurableBeanFactory)}
     * <p>
     * 托管到 Spring 上下文后，服务运行期间 Queue 被删除时会自动创建
     */
    @Bean("eventAMessageListenerContainer")
    public SimpleMessageListenerContainer eventAMessageListenerContainer(@Qualifier("mainRabbitmqConnectionFactory") CachingConnectionFactory mainRabbitmqConnectionFactory,
                                                                         @Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin,
                                                                         @Autowired HyggeEventAListener listener,
                                                                         @Qualifier("event-A") Queue queue) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(mainRabbitmqConnectionFactory);
        container.setAmqpAdmin(rabbitAdmin);

        container.setQueues(queue);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(listener);
        container.setPrefetchCount(1);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(1);
        return container;
    }

    /**
     * Queue 创建过程见 {@link EventBusConfig#deadEventBusHeadersExchange(RabbitAdmin, HeadersExchange, ConfigurableBeanFactory)}
     * <p>
     * 托管到 Spring 上下文后，服务运行期间 Queue 被删除时会自动创建
     */
    @Bean("eventBMessageListenerContainer")
    public SimpleMessageListenerContainer eventBMessageListenerContainer(@Qualifier("mainRabbitmqConnectionFactory") CachingConnectionFactory mainRabbitmqConnectionFactory,
                                                                         @Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin,
                                                                         @Autowired HyggeEventBListener listener,
                                                                         @Qualifier("event-B") Queue queue) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(mainRabbitmqConnectionFactory);
        container.setAmqpAdmin(rabbitAdmin);

        container.setQueues(queue);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(listener);
        container.setPrefetchCount(1);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(1);
        return container;
    }

    /**
     * Queue 创建过程见 {@link EventBusConfig#deadEventBusHeadersExchange(RabbitAdmin, HeadersExchange, ConfigurableBeanFactory)}
     * <p>
     * 托管到 Spring 上下文后，服务运行期间 Queue 被删除时会自动创建
     */
    @Bean("eventCMessageListenerContainer")
    public SimpleMessageListenerContainer eventCMessageListenerContainer(@Qualifier("mainRabbitmqConnectionFactory") CachingConnectionFactory mainRabbitmqConnectionFactory,
                                                                         @Qualifier("mainRabbitAdmin") RabbitAdmin rabbitAdmin,
                                                                         @Autowired HyggeEventCListener listener,
                                                                         @Qualifier("event-C") Queue queue) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(mainRabbitmqConnectionFactory);
        container.setAmqpAdmin(rabbitAdmin);

        container.setQueues(queue);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(listener);
        container.setPrefetchCount(1);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(1);
        return container;
    }
}
