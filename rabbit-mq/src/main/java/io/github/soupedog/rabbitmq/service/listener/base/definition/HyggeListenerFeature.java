package io.github.soupedog.rabbitmq.service.listener.base.definition;

import io.github.soupedog.rabbitmq.service.listener.base.HyggeRabbitMQMessageItem;
import io.github.soupedog.rabbitmq.service.listener.base.HyggeRabbitMqListenerContext;
import io.github.soupedog.rabbitmq.service.listener.base.StatusEnums;
import org.springframework.amqp.core.Message;

import java.util.Map;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public interface HyggeListenerFeature<T> extends HyggeListenerBaseFeature, HyggeListenerOperator {

    /**
     * 为目标消息标记 {@link StatusEnums#NEEDS_REQUEUE} 状态，该方法返回 {@link Boolean#TRUE} 代表需要触发 {@link HyggeListenerFeature#requeue(HyggeRabbitMqListenerContext)} 方法
     */
    default boolean isRequeueEnable(HyggeRabbitMqListenerContext<T> context) throws Exception {
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
    void requeue(HyggeRabbitMqListenerContext<T> context) throws Exception;

    /**
     * 将队列消息的 Headers 转化成字符串形式
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    String formatHeadersAsString(HyggeRabbitMqListenerContext<T> context, Map<String, Object> headers);

    /**
     * 将队列消息转化成字符串形式
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    String formatBodyAsString(HyggeRabbitMqListenerContext<T> context, Message message);

    /**
     * 将队列消息字符串形式转化成对象
     */
    T formatAsEntity(HyggeRabbitMqListenerContext<T> context, String messageStringVal) throws Exception;

    /**
     * 消息 headers 覆写，对 {@link HyggeRabbitMQMessageItem} 进行数据更新，常用于日志脱敏
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    default void messageHeadersOverwrite(HyggeRabbitMqListenerContext<T> context) {
        // do nothing by default
    }

    /**
     * 消息 body 覆写，对 {@link HyggeRabbitMQMessageItem} 进行数据更新，常用于日志脱敏用于日志脱敏
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    default void messageBodyOverwrite(HyggeRabbitMqListenerContext<T> context) {
        // do nothing by default
    }

    /**
     * 将消息信息输出到日志系统
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    void printMessageEntityLog(HyggeRabbitMqListenerContext<T> context, String prefixInfo);

    /**
     * 收到消息后需要做的业务处理
     */
    void onReceive(HyggeRabbitMqListenerContext<T> context, T messageEntity) throws Exception;

    /**
     * 对 {@link HyggeRabbitMQMessageItem#isAutoAckTriggered()} 为 {@link Boolean#FALSE} 的消息自动进行消费确认
     */
    void autoAck(HyggeRabbitMqListenerContext<T> context);

    /**
     * 消费完成后的业务扫尾处理，仅 {@link StatusEnums#ACK_SUCCESS} 或者 {@link StatusEnums#NACK_SUCCESS} 状态的消息会触发该方法
     */
    default void businessLogicFinishHook(HyggeRabbitMqListenerContext<T> context) throws Exception {
        // do nothing by default
    }

    /**
     * 若当前消息被标记为 {@link StatusEnums#NEEDS_RETRY}，尝试进行消息的重试
     */
    default void retryHook(HyggeRabbitMqListenerContext<T> context) throws Exception {
        // do nothing by default
    }

    /**
     * 链路追踪等技术性组件进行扫尾工作，无论什么情况都会尝试会执行的方法
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    default void finallyHook(HyggeRabbitMqListenerContext<T> context) {
        // do nothing by default
    }
}
