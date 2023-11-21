package io.github.soupedog.rabbitmq.service.listener.base;

import org.springframework.amqp.core.Message;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
public class HyggeRabbitMQMessageItem<T> {
    private Message message;
    private String headersStringVal;
    private String messageStringVal;
    private T messageEntity;
    private boolean autoAckTriggered;
    private StatusEnums status = StatusEnums.NEEDS_ACK;
    private Exception exception;

    private HyggeRabbitMQMessageItem() {
    }

    public HyggeRabbitMQMessageItem(Message message) {
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

    public boolean isAutoAckTriggered() {
        return autoAckTriggered;
    }

    public void setAutoAckTriggered(boolean autoAckTriggered) {
        this.autoAckTriggered = autoAckTriggered;
    }

    public StatusEnums getStatus() {
        return status;
    }

    public void setStatus(StatusEnums status) {
        this.status = status;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public boolean isExceptionOccurred() {
        return !isNoExceptionOccurred();
    }

    public boolean isNoExceptionOccurred() {
        return exception == null;
    }

    /**
     * 防止 NACK 状态分散各个 try-catch 中，仅需在 ack 阶段前调用该方法进行检测并赋值
     */
    public void nackStatusCheckAndReset() {
        // 仅允许 NEEDS_ACK 状态发送异常后迁移到 NEEDS_NACK 状态
        if (!isAutoAckTriggered() && statusExpected(StatusEnums.NEEDS_ACK) && isExceptionOccurred()) {
            this.status = StatusEnums.NEEDS_NACK;
        }
    }

    public boolean statusExpected(StatusEnums... statusEnums) {
        for (StatusEnums action : statusEnums) {
            if (action.equals(this.status)) {
                return true;
            }
        }
        return false;
    }
}
