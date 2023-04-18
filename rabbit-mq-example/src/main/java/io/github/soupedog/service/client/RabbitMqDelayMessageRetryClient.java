package io.github.soupedog.service.client;

import hygge.web.template.HyggeWebUtilContainer;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.time.Duration;

/**
 * 延迟队列方式进行消息重试
 *
 * @author Xavier
 * @date 2023/4/18
 * @since 1.0
 */
public class RabbitMqDelayMessageRetryClient extends HyggeWebUtilContainer {
    private RabbitAdmin rabbitAdmin;
    private ConfigurableBeanFactory factory;
    private String staticRetryExchangeName;

    public RabbitMqDelayMessageRetryClient(RabbitAdmin rabbitAdmin, ConfigurableBeanFactory factory) {
        this.rabbitAdmin = rabbitAdmin;
        this.factory = factory;
    }

    public String getDelayExchangeName(String rawExchange, String rawRoutingKey) {
        if (parameterHelper.isNotEmpty(staticRetryExchangeName)) {
            return staticRetryExchangeName;
        }

        return String.format("hygge-retry-%s-%s", rawExchange, rawRoutingKey);
    }

    public String getDelayQueueName(String rawQueueName, String rawExchange, String rawRoutingKey, Configuration configuration) {
        if (parameterHelper.isNotEmpty(rawQueueName)) {
            return String.format("%s-%s-%s", rawQueueName, configuration.getMaxRetryAsString(), configuration.getRetryInterval().toString());
        }

        return String.format("hygge-retry-%s-%s-%s-%s", rawExchange, rawRoutingKey, configuration.getMaxRetryAsString(), configuration.getRetryInterval().toString());
    }

    public void initDelayResource(String rawQueueName, String rawExchange, String rawRoutingKey, Configuration configuration) {
        String queueName = getDelayQueueName(rawQueueName, rawExchange, rawRoutingKey, configuration);

        Queue delayQueue = QueueBuilder
                .durable(queueName)
                .ttl(parameterHelper.integerFormat("ttl", configuration.getRetryInterval().toMillis()))
                .deadLetterExchange(rawExchange)
                .deadLetterRoutingKey(rawRoutingKey)
                .build();
        delayQueue.setAdminsThatShouldDeclare(rabbitAdmin);

        rabbitAdmin.declareQueue(delayQueue);
        factory.registerSingleton(queueName, delayQueue);

        String delayExchangeName = getDelayExchangeName(rawExchange, rawRoutingKey);

        TopicExchange delayExchange = new TopicExchange(delayExchangeName);
        delayExchange.setAdminsThatShouldDeclare(rabbitAdmin);
        rabbitAdmin.declareExchange(delayExchange);
        factory.registerSingleton(delayExchangeName, delayExchange);

        Binding binding = BindingBuilder.bind(delayQueue).to(delayExchange).with(rawRoutingKey);
        binding.setAdminsThatShouldDeclare(rabbitAdmin);
        rabbitAdmin.declareBinding(binding);

        factory.registerSingleton(delayExchangeName + "-to-" + delayQueue, binding);
    }

    public static class Configuration {
        /**
         * 最大重试次数
         */
        private int maxRetry = 2;
        /**
         * 消息最小重试间隔
         */
        private Duration retryInterval = Duration.ofMinutes(5);
        /**
         * 最大存活时间(negative 则代表永久有效)
         */
        private Duration ttl = Duration.ofMinutes(-1);

        public String getMaxRetryAsString() {
            if (maxRetry <= 0) {
                return "infinity";
            }
            return Integer.valueOf(maxRetry).toString();
        }

        public int getMaxRetry() {
            return maxRetry;
        }

        public void setMaxRetry(int maxRetry) {
            this.maxRetry = maxRetry;
        }

        public Duration getRetryInterval() {
            return retryInterval;
        }

        public void setRetryInterval(Duration retryInterval) {
            this.retryInterval = retryInterval;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }
}
