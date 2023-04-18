package io.github.soupedog.listener.base;

import com.rabbitmq.client.Channel;
import hygge.commons.template.container.base.AbstractHyggeContext;
import io.github.soupedog.listener.base.definition.HyggeRabbitMqListenerContextFeature;
import org.springframework.boot.logging.LogLevel;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public class HyggeRabbitMqListenerContext<T> extends AbstractHyggeContext<String> implements HyggeRabbitMqListenerContextFeature {
    private long startTs = System.currentTimeMillis();
    private LogLevel loglevel = LogLevel.INFO;
    private Channel channel;
    private HyggeRabbitMQMessageItem<T> rwaMessage;

    @Override
    public long getStartTs() {
        return this.startTs;
    }

    @Override
    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    @Override
    public LogLevel getLoglevel() {
        return this.loglevel;
    }

    @Override
    public void setLoglevelIntelligently(LogLevel loglevel) {
        if (loglevel.ordinal() > this.loglevel.ordinal()) {
            this.loglevel = loglevel;
        }
    }

    @Override
    public void setLoglevel(LogLevel loglevel) {
        this.loglevel = loglevel;
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public boolean isExceptionOccurred() {
        return this.rwaMessage.isExceptionOccurred();
    }

    @Override
    public boolean isNoExceptionOccurred() {
        return this.rwaMessage.isNoExceptionOccurred();
    }

    public HyggeRabbitMQMessageItem<T> getRwaMessage() {
        return rwaMessage;
    }

    public void setRwaMessage(HyggeRabbitMQMessageItem<T> rwaMessage) {
        this.rwaMessage = rwaMessage;
    }
}
