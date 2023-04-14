package io.github.soupedog.listener.base;

import com.rabbitmq.client.Channel;
import hygge.commons.template.container.base.AbstractHyggeContext;
import org.springframework.boot.logging.LogLevel;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public class HyggeRabbitMqListenerContext<T> extends AbstractHyggeContext<String> {
    private long startTs = System.currentTimeMillis();
    private LogLevel loglevel = LogLevel.INFO;
    private T rwaMessage;
    private Channel channel;
    private boolean autoAckTriggered = false;
    private boolean retryable = true;
    private boolean businessLogicFinishEnable = false;
    private Throwable throwable;

    public long getStartTs() {
        return startTs;
    }

    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    public LogLevel getLoglevel() {
        return loglevel;
    }

    public void setLoglevel(LogLevel loglevel) {
        this.loglevel = loglevel;
    }

    public T getRwaMessage() {
        return rwaMessage;
    }

    public void setRwaMessage(T rwaMessage) {
        this.rwaMessage = rwaMessage;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public boolean isAutoAckTriggered() {
        return autoAckTriggered;
    }

    public void setAutoAckTriggered(boolean autoAckTriggered) {
        this.autoAckTriggered = autoAckTriggered;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }

    public boolean isBusinessLogicFinishEnable() {
        return businessLogicFinishEnable;
    }

    public void setBusinessLogicFinishEnable(boolean businessLogicFinishEnable) {
        this.businessLogicFinishEnable = businessLogicFinishEnable;
    }

    public boolean isExceptionOccurred() {
        return !isNoExceptionOccurred();
    }

    public boolean isNoExceptionOccurred() {
        return throwable == null;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
