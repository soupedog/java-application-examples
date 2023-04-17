package io.github.soupedog.listener.base;

import org.springframework.amqp.core.Message;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
public class HyggeBatchMessageItem<T> {
    private Message message;
    private String headersStringVal;
    private String messageStringVal;
    private T messageEntity;
    private ActionEnum action = ActionEnum.NEEDS_ACK;
    private boolean autoAckTriggered;
    private Throwable throwable;

    private HyggeBatchMessageItem() {
    }

    public HyggeBatchMessageItem(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getHeadersStringVal() {
        return headersStringVal;
    }

    public void setHeadersStringVal(String headersStringVal) {
        this.headersStringVal = headersStringVal;
    }

    public String getMessageStringVal() {
        return messageStringVal;
    }

    public void setMessageStringVal(String messageStringVal) {
        this.messageStringVal = messageStringVal;
    }

    public T getMessageEntity() {
        return messageEntity;
    }

    public void setMessageEntity(T messageEntity) {
        this.messageEntity = messageEntity;
    }

    public ActionEnum getAction() {
        return action;
    }

    public void setAction(ActionEnum action) {
        this.action = action;
    }

    public boolean isAutoAckTriggered() {
        return autoAckTriggered;
    }

    public void setAutoAckTriggered(boolean autoAckTriggered) {
        this.autoAckTriggered = autoAckTriggered;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public boolean isExceptionOccurred() {
        return !isNoExceptionOccurred();
    }

    public boolean isNoExceptionOccurred() {
        return throwable == null;
    }
}
