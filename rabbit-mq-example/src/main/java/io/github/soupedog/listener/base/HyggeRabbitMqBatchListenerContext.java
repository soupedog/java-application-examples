package io.github.soupedog.listener.base;

import com.rabbitmq.client.Channel;
import hygge.commons.template.container.base.AbstractHyggeContext;
import io.github.soupedog.listener.base.definition.HyggeRabbitMqListenerContextFeature;
import org.springframework.boot.logging.LogLevel;

import java.util.List;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
public class HyggeRabbitMqBatchListenerContext<T> extends AbstractHyggeContext<String> implements HyggeRabbitMqListenerContextFeature {
    private long startTs = System.currentTimeMillis();
    private LogLevel loglevel = LogLevel.INFO;
    private Channel channel;
    private List<HyggeRabbitMQMessageItem<T>> rawMessageList;
    private long maxDeliveryTag;

    @Override
    public long getStartTs() {
        return startTs;
    }

    @Override
    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    @Override
    public LogLevel getLoglevel() {
        return loglevel;
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
        return channel;
    }

    @Override
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public boolean isExceptionOccurred() {
        return rawMessageList.stream().anyMatch(item -> item.getException() != null);
    }

    @Override
    public boolean isNoExceptionOccurred() {
        return !isExceptionOccurred();
    }

    public List<HyggeRabbitMQMessageItem<T>> getRawMessageList() {
        return rawMessageList;
    }

    public void setRawMessageList(List<HyggeRabbitMQMessageItem<T>> rawMessageList) {
        this.rawMessageList = rawMessageList;
    }

    public long getMaxDeliveryTag() {
        return maxDeliveryTag;
    }

    public void setMaxDeliveryTag(long maxDeliveryTag) {
        this.maxDeliveryTag = maxDeliveryTag;
    }

    public void setMaxDeliveryTagIntelligently(long maxDeliveryTag) {
        if (maxDeliveryTag > this.maxDeliveryTag) {
            this.maxDeliveryTag = maxDeliveryTag;
        }
    }

    public MultipleAckInfo analyzeMultipleAckInfo() {
        MultipleAckInfo result = new MultipleAckInfo(false, null);

        rawMessageList.forEach(HyggeRabbitMQMessageItem::nackStatusCheckAndReset);

        HyggeRabbitMQMessageItem<T> firstItem = rawMessageList.get(0);

        if (!firstItem.getStatus().equals(StatusEnums.NEEDS_ACK) && !firstItem.getStatus().equals(StatusEnums.NEEDS_NACK)) {
            return result;
        }

        if (rawMessageList.stream().allMatch(messageItem -> !messageItem.isAutoAckTriggered() && messageItem.getStatus().equals(firstItem.getStatus()))) {
            return new MultipleAckInfo(true, firstItem.getStatus());
        }
        return result;
    }

    public static class MultipleAckInfo {
        private boolean multipleAckEnable;
        private StatusEnums action;

        public MultipleAckInfo(boolean multipleAckEnable, StatusEnums action) {
            this.multipleAckEnable = multipleAckEnable;
            this.action = action;
        }

        public boolean isMultipleAckEnable() {
            return multipleAckEnable;
        }

        public void setMultipleAckEnable(boolean multipleAckEnable) {
            this.multipleAckEnable = multipleAckEnable;
        }

        public StatusEnums getAction() {
            return action;
        }

        public void setAction(StatusEnums action) {
            this.action = action;
        }
    }
}
