package io.github.soupedog.listener.base.definition;

import io.github.soupedog.listener.base.ActionEnum;
import io.github.soupedog.listener.base.HyggeBatchMessageItem;
import io.github.soupedog.listener.base.HyggeRabbitMqBatchListenerContext;
import org.springframework.boot.logging.LogLevel;

import java.util.List;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
public interface HyggeBatchListenerFeature<T> extends HyggeListenerBaseFeature, HyggeListenerOperator {

    /**
     * 是否要将当前消息发送回原队列
     * <p>
     * 常用于标记接收的消息不属于当前实例，不应进行消费并尝试将当前消息恢复到等效于被消费前的状态
     */
    default boolean isRequeueEnable(HyggeRabbitMqBatchListenerContext<T> context) {
        return false;
    }

    /**
     * 进行 requeue 并将无需 requeue 操作的消息进行返回
     *
     * <p>
     * 该方法默认实现的机制是先 ack 当前消息，再将当前消息丢回队尾，可能产生消息丢失
     */
    List<HyggeBatchMessageItem<T>> requeue(HyggeRabbitMqBatchListenerContext<T> context);

    /**
     * 将队列消息 Headers 转化成字符串形式
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    String formatMessageHeadersAsString(HyggeRabbitMqBatchListenerContext<T> context, HyggeBatchMessageItem<T> messageItem);

    /**
     * 将队列消息转化成字符串形式
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    String formatMessageBodyAsString(HyggeRabbitMqBatchListenerContext<T> context, HyggeBatchMessageItem<T> messageItem);

    /**
     * 将 HyggeBatchMessageItem 中的队列消息字符串形式转化成对象并存入 HyggeBatchMessageItem
     */
    void formatMessageAsEntity(HyggeRabbitMqBatchListenerContext<T> context);

    /**
     * 消息 headers 覆写，用于日志脱敏
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    default String messageHeadersOverwrite(HyggeRabbitMqBatchListenerContext<T> context, HyggeBatchMessageItem<T> messageItem) {
        return messageItem.getHeadersStringVal();
    }

    /**
     * 消息 body 覆写，用于日志脱敏
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    default String messageBodyOverwrite(HyggeRabbitMqBatchListenerContext<T> context, HyggeBatchMessageItem<T> messageItem) {
        return messageItem.getMessageStringVal();
    }

    /**
     * 打印接收到的消息信息
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    void printMessageEntityLog(LogLevel logLevel, List<HyggeBatchMessageItem<T>> messageList, String prefixInfo);

    /**
     * 收到消息后需要做的业务处理
     */
    void onReceive(HyggeRabbitMqBatchListenerContext<T> context);

    /**
     * 根据 {@link HyggeBatchMessageItem#isAutoAckTriggered()}、{@link HyggeBatchMessageItem#getAction()} ，进行自动 ACK 并更新 ACK 结果到对应 {@link HyggeBatchMessageItem} 中
     */
    void autoAck(HyggeRabbitMqBatchListenerContext<T> context);

    /**
     * 将该步骤之前被标记了 {@link ActionEnum#NEEDS_RETRY} 的消息筛选出来，并进行重试
     */
    default void retryHook(HyggeRabbitMqBatchListenerContext<T> context, List<HyggeBatchMessageItem<T>> needsRetryList) {
        // do nothing by default
    }

    /**
     * 业务处理结束时的一些扫尾工作，在自动 ack 执行完成后，将该步骤之前被标记了 {@link ActionEnum#ACK_SUCCESS} 或 {@link ActionEnum#NACK_SUCCESS} 的消息筛选出来进行业务扫尾处理工作
     */
    default void businessLogicFinishHook(HyggeRabbitMqBatchListenerContext<T> context, List<HyggeBatchMessageItem<T>> needsBusinessLogicFinishList) {
        // do nothing by default
    }

    /**
     * 链路追踪等技术性组件进行扫尾工作，位于整个消费逻辑的最后一步执行，无论什么情况都会尝试会执行的方法
     */
    default void finallyHook(HyggeRabbitMqBatchListenerContext<T> context) {
        // do nothing by default
    }
}
