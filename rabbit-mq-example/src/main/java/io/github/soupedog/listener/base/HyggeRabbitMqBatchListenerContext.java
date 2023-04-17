package io.github.soupedog.listener.base;

import com.rabbitmq.client.Channel;
import hygge.commons.template.container.base.AbstractHyggeContext;
import org.springframework.boot.logging.LogLevel;

import java.util.List;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
public class HyggeRabbitMqBatchListenerContext<T> extends AbstractHyggeContext<String> {
    private long startTs = System.currentTimeMillis();
    private LogLevel loglevel = LogLevel.INFO;
    private Channel channel;
    private List<HyggeBatchMessageItem<T>> rawMessageList;
    private long maxDeliveryTag;

    public long getStartTs() {
        return startTs;
    }

    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    public LogLevel getLoglevel() {
        return loglevel;
    }

    public void setLoglevelIntelligently(LogLevel loglevel) {
        if (loglevel.ordinal() > this.loglevel.ordinal()) {
            this.loglevel = loglevel;
        }
    }

    public void setLoglevel(LogLevel loglevel) {
        this.loglevel = loglevel;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public List<HyggeBatchMessageItem<T>> getRawMessageList() {
        return rawMessageList;
    }

    public void setRawMessageList(List<HyggeBatchMessageItem<T>> rawMessageList) {
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

    public boolean isExceptionOccurred() {
        return rawMessageList.stream().anyMatch(item -> item.getThrowable() != null);
    }

    public boolean isNoExceptionOccurred() {
        return !isExceptionOccurred();
    }

    public MultipleAckInfo analyzeMultipleAckInfo() {
        MultipleAckInfo result = new MultipleAckInfo(false, null);
        if (rawMessageList == null || rawMessageList.isEmpty()) {
            return result;
        }

        HyggeBatchMessageItem<T> firstItem = rawMessageList.get(0);

        if (!firstItem.getAction().equals(ActionEnum.NEEDS_ACK) && !firstItem.getAction().equals(ActionEnum.NEEDS_NACK)) {
            return result;
        }

        if (rawMessageList.stream().allMatch(messageItem -> messageItem.getAction().equals(firstItem.getAction()))) {
            return new MultipleAckInfo(true, firstItem.getAction());
        }
        return result;
    }

    public static class MultipleAckInfo {
        private boolean multipleAckEnable;
        private ActionEnum action;

        public MultipleAckInfo(boolean multipleAckEnable, ActionEnum action) {
            this.multipleAckEnable = multipleAckEnable;
            this.action = action;
        }

        public boolean isMultipleAckEnable() {
            return multipleAckEnable;
        }

        public void setMultipleAckEnable(boolean multipleAckEnable) {
            this.multipleAckEnable = multipleAckEnable;
        }

        public ActionEnum getAction() {
            return action;
        }

        public void setAction(ActionEnum action) {
            this.action = action;
        }
    }
}
