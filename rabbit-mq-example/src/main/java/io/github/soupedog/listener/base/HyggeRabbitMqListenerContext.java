package io.github.soupedog.listener.base;

import com.rabbitmq.client.Channel;
import hygge.commons.template.container.base.AbstractHyggeContext;
import org.springframework.amqp.core.Message;
import org.springframework.boot.logging.LogLevel;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public class HyggeRabbitMqListenerContext extends AbstractHyggeContext<String> {
    private long startTs = System.currentTimeMillis();
    private LogLevel loglevel = LogLevel.INFO;
    private Message message;
    private Channel channel;
    private boolean autoAckTriggered = false;
    private boolean retryable = true;
    private boolean finishHookEnable = false;
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

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
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

    public boolean isFinishHookEnable() {
        return finishHookEnable;
    }

    public void setFinishHookEnable(boolean finishHookEnable) {
        this.finishHookEnable = finishHookEnable;
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
