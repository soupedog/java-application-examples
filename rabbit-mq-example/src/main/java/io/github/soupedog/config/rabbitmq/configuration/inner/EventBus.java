package io.github.soupedog.config.rabbitmq.configuration.inner;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@ConfigurationProperties(prefix = "test.demo.rabbit.event-bus")
public class EventBus {
    public String exchange;
    public String deadExchange;
    public ArrayList<String> eventNames;
    private Integer ttlMillisecond;
    public String deadMessageQueue;

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
