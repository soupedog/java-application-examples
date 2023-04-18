package io.github.soupedog.listener.base;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
public enum StatusEnums {
    /**
     * 重回队列相关状态：待执行、执行成功一半(已 ACK 等待拷贝重新发送)、重回队列成功、重回队列失败
     */
    NEEDS_REQUEUE, REQUEUE_HALF_SUCCESS, REQUEUE_SUCCESS, REQUEUE_FAILURE,
    /**
     * 正常消费相关：待执行、ACK 成功、ACK 引发的 UN_ACKED
     */
    NEEDS_ACK, ACK_SUCCESS, ACK_UN_ACKED,
    /**
     * NACK 相关：待执行(在自动 ack 阶段之前，默认存在异常就是该状态)、NACK 成功、NACK 引发的 UN_ACKED
     */
    NEEDS_NACK, NACK_SUCCESS, NACK_UN_ACKED,
    /**
     * 重试相关:待执行、执行成功一半(已 ACK 等待拷贝重新发送)、重试成功、重试失败
     */
    NEEDS_RETRY, RETRY_HALF_SUCCESS, RETRY_SUCCESS, RETRY_FAILURE,
}
