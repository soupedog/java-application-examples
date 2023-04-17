package io.github.soupedog.listener.base;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
public enum ActionEnum {
    /**
     * 重回队列相关状态
     */
    NEEDS_REQUEUE, REQUEUE_SUCCESS, REQUEUE_UN_ACKED,
    /**
     * 正常消费相关
     */
    NEEDS_ACK, ACK_SUCCESS, ACK_UN_ACKED,
    /**
     * NACK 相关
     */
    NEEDS_NACK, NACK_SUCCESS, NACK_UN_ACKED,
    /**
     * 重试相关
     */
    NEEDS_RETRY, RETRY_SUCCESS, RETRY_FAILURE,
}
