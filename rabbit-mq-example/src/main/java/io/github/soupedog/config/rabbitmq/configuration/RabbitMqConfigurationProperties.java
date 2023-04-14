package io.github.soupedog.config.rabbitmq.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@ConfigurationProperties(prefix = "test.demo.rabbit")
public class RabbitMqConfigurationProperties {
    private String environmentName;
    private String address;
    private String virtualHost;
    private String userName;
    private String password;
    private Main main;
    private EventBus eventBus;
    private Annotation annotation;
    private Batch batch;

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    /**
     * @author Xavier
     * @date 2023/4/14
     * @since 1.0
     */
    public static class Main {
        private String exchange;
        private String routingKey;

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }
    }

    /**
     * @author Xavier
     * @date 2023/4/14
     * @since 1.0
     */
    public static class EventBus {
        public String exchange;
        public String deadExchange;
        public ArrayList<String> eventNames;
        private Integer ttlMillisecond;

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public ArrayList<String> getEventNames() {
            return eventNames;
        }

        public String getDeadExchange() {
            return deadExchange;
        }

        public void setDeadExchange(String deadExchange) {
            this.deadExchange = deadExchange;
        }

        public void setEventNames(ArrayList<String> eventNames) {
            this.eventNames = eventNames;
        }

        public Integer getTtlMillisecond() {
            return ttlMillisecond;
        }

        public void setTtlMillisecond(Integer ttlMillisecond) {
            this.ttlMillisecond = ttlMillisecond;
        }
    }

    /**
     * @author Xavier
     * @date 2023/4/14
     * @since 1.0
     */
    public static class Annotation {
        private String exchange;
        private String routingKey;
        private Integer ttlMillisecond;
        private String deadExchange;
        private String deadRoutingKey;
        private Integer deadTtlMillisecond;

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }

        public Integer getTtlMillisecond() {
            return ttlMillisecond;
        }

        public void setTtlMillisecond(Integer ttlMillisecond) {
            this.ttlMillisecond = ttlMillisecond;
        }

        public String getDeadExchange() {
            return deadExchange;
        }

        public void setDeadExchange(String deadExchange) {
            this.deadExchange = deadExchange;
        }

        public String getDeadRoutingKey() {
            return deadRoutingKey;
        }

        public void setDeadRoutingKey(String deadRoutingKey) {
            this.deadRoutingKey = deadRoutingKey;
        }

        public Integer getDeadTtlMillisecond() {
            return deadTtlMillisecond;
        }

        public void setDeadTtlMillisecond(Integer deadTtlMillisecond) {
            this.deadTtlMillisecond = deadTtlMillisecond;
        }
    }

    /**
     * @author Xavier
     * @date 2023/4/14
     * @since 1.0
     */
    public static class Batch {
        private String exchange;
        private String routingKey;
        private Integer batchSize;

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }

        public Integer getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(Integer batchSize) {
            this.batchSize = batchSize;
        }
    }
}
