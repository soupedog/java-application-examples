package io.github.soupedog.config.rabbitmq.configuration.inner;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@ConfigurationProperties(prefix = "test.demo.rabbit.annotation")
public class Annotation {
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
