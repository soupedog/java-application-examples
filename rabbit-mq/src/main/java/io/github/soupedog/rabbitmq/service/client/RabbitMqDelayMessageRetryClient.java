package io.github.soupedog.rabbitmq.service.client;

import hygge.web.template.HyggeWebUtilContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.time.Duration;

/**
 * 延迟队列方式进行消息重试
 *
 * @author Xavier
 * @date 2023/4/18
 * @since 1.0
 */
public class RabbitMqDelayMessageRetryClient extends HyggeWebUtilContainer {
    private static final Logger log = LoggerFactory.getLogger(RabbitMqDelayMessageRetryClient.class);
    protected String prefix = "hygge-retry-";
    private RabbitAdmin rabbitAdmin;
    /**
     * 该值为空则会为每个需要重试机制的 queue 单独创建 exchange 进行延迟队列重试行为，以防止共享 exchange 引发广播机制而通知多个 queue
     */
    private String staticRetryExchangeName;

    public RabbitMqDelayMessageRetryClient(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public String getDelayExchangeName(String rawExchange, Configuration configuration) {
        if (parameterHelper.isNotEmpty(staticRetryExchangeName)) {
            return staticRetryExchangeName;
        }

        return String.format(prefix + "%s", rawExchange);
    }

    public String getDelayExchangeName(Queue rawQueue, Configuration configuration) {
        if (parameterHelper.isNotEmpty(staticRetryExchangeName)) {
            return staticRetryExchangeName;
        }

        return String.format(prefix + "extra-%s", rawQueue.getName());
    }

    public String getDelayRoutingKey(String rawRoutingKey, Configuration configuration) {
        return String.format(prefix + "%s-%s-%s", rawRoutingKey, configuration.getMaxRetryAsString(), configuration.getRetryInterval().toString());
    }

    public String getDelayRoutingKey(Queue rawQueue, Configuration configuration) {
        return String.format(prefix + "extra-%s-%s-%s", rawQueue.getName(), configuration.getMaxRetryAsString(), configuration.getRetryInterval().toString());
    }

    public String getDelayQueueName(String rawQueueName, String rawExchange, String rawRoutingKey, Configuration configuration) {
        if (parameterHelper.isNotEmpty(rawQueueName)) {
            return String.format("%s-%s-%s", rawQueueName, configuration.getMaxRetryAsString(), configuration.getRetryInterval().toString());
        }

        return String.format(prefix + "%s-%s-%s-%s", rawExchange, rawRoutingKey, configuration.getMaxRetryAsString(), configuration.getRetryInterval().toString());
    }

    protected String getDelayQueueName(Queue rawQueue, Configuration configuration) {
        return String.format("%s-%s-%s", rawQueue.getName(), configuration.getMaxRetryAsString(), configuration.getRetryInterval().toString());
    }

    public String getDelayRoutingKeyToDelay(Queue rawQueue, Configuration configuration) {
        return getDelayRoutingKey(rawQueue, configuration) + "1";
    }

    public String getDelayRoutingKeyToRaw(Queue rawQueue, Configuration configuration) {
        return getDelayRoutingKey(rawQueue, configuration) + "2";
    }

    public void initDelayResource(String rawQueueName, String rawExchange, String rawRoutingKey, Configuration configuration) {
        String delayExchangeName = getDelayExchangeName(rawExchange, configuration);

        TopicExchange delayExchange = new TopicExchange(delayExchangeName);
        delayExchange.setAdminsThatShouldDeclare(rabbitAdmin);
        rabbitAdmin.declareExchange(delayExchange);

        String delayRoutingKey = getDelayRoutingKey(rawRoutingKey, configuration);

        String delayQueueName = getDelayQueueName(rawQueueName, rawExchange, rawRoutingKey, configuration);

        Queue delayQueue = QueueBuilder
                .durable(delayQueueName)
                .ttl(parameterHelper.integerFormat("ttl", configuration.getRetryInterval().toMillis()))
                .deadLetterExchange(rawExchange)
                .deadLetterRoutingKey(rawRoutingKey)
                .build();
        delayQueue.setAdminsThatShouldDeclare(rabbitAdmin);

        rabbitAdmin.declareQueue(delayQueue);

        Binding binding = BindingBuilder.bind(delayQueue).to(delayExchange).with(delayRoutingKey);
        binding.setAdminsThatShouldDeclare(rabbitAdmin);
        rabbitAdmin.declareBinding(binding);

        log.info("Init retry resource. exchange:{} routingKey:{} delayQueueName:{} ttl:{}", delayExchangeName, delayRoutingKey, delayQueueName, configuration.retryInterval.toString());
    }

    /**
     * 需要重试的消息位于的队列是广播形式，需要构建一套细颗粒度的消息分发绑定
     */
    public void initDelayResource(Queue rawQueue, Configuration configuration) {
        String delayExchangeName = getDelayExchangeName(rawQueue, configuration);

        TopicExchange delayExchange = new TopicExchange(delayExchangeName);
        delayExchange.setAdminsThatShouldDeclare(rabbitAdmin);
        rabbitAdmin.declareExchange(delayExchange);

        String delayRoutingKey = getDelayRoutingKeyToDelay(rawQueue, configuration);
        String delayRoutingKeyToRaw = getDelayRoutingKeyToRaw(rawQueue, configuration);

        String delayQueueName = getDelayQueueName(rawQueue, configuration);

        Queue delayQueue = QueueBuilder
                .durable(delayQueueName)
                .ttl(parameterHelper.integerFormat("ttl", configuration.getRetryInterval().toMillis()))
                .deadLetterExchange(delayExchangeName)
                .deadLetterRoutingKey(delayRoutingKeyToRaw)
                .build();
        delayQueue.setAdminsThatShouldDeclare(rabbitAdmin);

        rabbitAdmin.declareQueue(delayQueue);

        // delayExchange → delayQueue
        Binding binding1 = BindingBuilder.bind(delayQueue).to(delayExchange).with(delayRoutingKey);
        binding1.setAdminsThatShouldDeclare(rabbitAdmin);
        rabbitAdmin.declareBinding(binding1);

        // delayExchange → rawQueue
        Binding binding2 = BindingBuilder.bind(rawQueue).to(delayExchange).with(delayRoutingKeyToRaw);
        binding2.setAdminsThatShouldDeclare(rabbitAdmin);
        rabbitAdmin.declareBinding(binding2);

        log.info("Init retry resource. exchange:{} routingKey:{} delayQueueName:{} ttl:{}", delayExchangeName, delayRoutingKey, delayQueueName, configuration.retryInterval.toString());
    }

    public RabbitAdmin getRabbitAdmin() {
        return rabbitAdmin;
    }

    public void setRabbitAdmin(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public String getStaticRetryExchangeName() {
        return staticRetryExchangeName;
    }

    public void setStaticRetryExchangeName(String staticRetryExchangeName) {
        this.staticRetryExchangeName = staticRetryExchangeName;
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
        private Duration ttl = Duration.ofSeconds(-1);

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
