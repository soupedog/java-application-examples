package io.github.soupedog.listener.base.definition;

import io.github.soupedog.listener.base.HyggeRabbitMQMessageItem;
import io.github.soupedog.listener.base.HyggeRabbitMqBatchListenerContext;
import io.github.soupedog.listener.base.StatusEnums;

import java.util.List;

/**
 * 与 {@link HyggeListenerFeature} 相似，但批量功能需要实现的方法均不允许抛出异常，故方法中不再赘述，异常等信息需要存入单条消息对应的 {@link HyggeRabbitMQMessageItem<T>}
 *
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
public interface HyggeListenerBatchFeature<T> extends HyggeListenerBaseFeature, HyggeListenerOperator {

    /**
     * 为目标消息标记 {@link StatusEnums#NEEDS_REQUEUE} 状态，该方法返回 {@link Boolean#TRUE} 代表需要触发 {@link HyggeListenerBatchFeature#requeue(HyggeRabbitMqBatchListenerContext)} 方法
     */
    default boolean isRequeueEnable(HyggeRabbitMqBatchListenerContext<T> context) {
        return false;
    }

    /**
     * 尝试将标记了 {@link StatusEnums#NEEDS_REQUEUE} 的消息恢复到等效于被消费前的状态
     * <p>
     * 注意：默认实现存在消息丢失的风险
     * <pre>
     *     ① 先将当前消息进行 ACK
     *     ② 检查重回队列次数是否超出上限
     *     ③ 未超出重回队列次数上限则拷贝当前消息并发回原队列尾部
     * </pre>
     */
    List<HyggeRabbitMQMessageItem<T>> requeue(HyggeRabbitMqBatchListenerContext<T> context);

    /**
     * 将队列消息 Headers 转化成字符串形式
     */
    String formatMessageHeadersAsString(HyggeRabbitMqBatchListenerContext<T> context, HyggeRabbitMQMessageItem<T> messageItem);

    /**
     * 将队列消息转化成字符串形式
     */
    String formatMessageBodyAsString(HyggeRabbitMqBatchListenerContext<T> context, HyggeRabbitMQMessageItem<T> messageItem);

    /**
     * 将队列消息字符串形式转化成对象，并封装成 {@link HyggeRabbitMQMessageItem<T>} 存入 {@link HyggeRabbitMqBatchListenerContext#getRawMessageList()}
     */
    void formatMessageAsEntity(HyggeRabbitMqBatchListenerContext<T> context);

    /**
     * 消息 headers 覆写，对 {@link HyggeRabbitMQMessageItem} 进行数据更新，常用于日志脱敏
     */
    default void messageHeadersOverwrite(HyggeRabbitMqBatchListenerContext<T> context, HyggeRabbitMQMessageItem<T> messageItem) {
        // do nothing by default
    }

    /**
     * 消息 body 覆写，对 {@link HyggeRabbitMQMessageItem} 进行数据更新，常用于日志脱敏用于日志脱敏
     */
    default void messageBodyOverwrite(HyggeRabbitMqBatchListenerContext<T> context, HyggeRabbitMQMessageItem<T> messageItem) {
        // do nothing by default
    }

    /**
     * 将消息信息输出到日志系统
     */
    void printMessageEntityLog(HyggeRabbitMqBatchListenerContext<T> context, List<HyggeRabbitMQMessageItem<T>> messageList, String prefixInfo);

    /**
     * 收到消息后需要做的业务处理
     */
    void onReceive(HyggeRabbitMqBatchListenerContext<T> context);

    /**
     * 对 {@link HyggeRabbitMQMessageItem#isAutoAckTriggered()} 为 {@link Boolean#FALSE} 的消息自动进行消费确认
     */
    void autoAck(HyggeRabbitMqBatchListenerContext<T> context);

    /**
     * 筛选出被标记为 {@link StatusEnums#NEEDS_RETRY} 的消息，随后进行消息的重试
     */
    default void retryHook(HyggeRabbitMqBatchListenerContext<T> context, List<HyggeRabbitMQMessageItem<T>> needsRetryList) {
        // do nothing by default
    }

    /**
     * 消费完成后的业务扫尾处理，仅 {@link StatusEnums#ACK_SUCCESS} 或者 {@link StatusEnums#NACK_SUCCESS} 状态的消息会触发该方法
     */
    default void businessLogicFinishHook(HyggeRabbitMqBatchListenerContext<T> context, List<HyggeRabbitMQMessageItem<T>> needsBusinessLogicFinishList) {
        // do nothing by default
    }

    /**
     * 链路追踪等技术性组件进行扫尾工作，位于整个消费逻辑的最后一步执行，无论什么情况都会尝试会执行的方法
     */
    default void finallyHook(HyggeRabbitMqBatchListenerContext<T> context) {
        // do nothing by default
    }
}
