package io.github.soupedog.listener.base.definition;

import io.github.soupedog.listener.base.HyggeRabbitMqListenerContext;
import org.springframework.amqp.core.Message;

import java.util.Map;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public interface HyggeListenerFeature<T> extends HyggeListenerBaseFeature, HyggeListenerOperator {

    /**
     * 是否要将当前消息发送回原队列
     * <p>
     * 常用于标记接收的消息不属于当前实例，不应进行消费并尝试将当前消息恢复到等效于被消费前的状态
     */
    default boolean isRequeueEnable(HyggeRabbitMqListenerContext<Message> context) throws Exception {
        return false;
    }

    /**
     * 如果 {@link HyggeListenerFeature#isRequeueEnable(HyggeRabbitMqListenerContext)} 为 true，则将当前消息恢复到等效于被消费前的状态
     * <p>
     * 该方法默认实现的机制是先 ack 当前消息，再将当前消息丢回队尾，可能产生消息丢失
     */
    void requeue(HyggeRabbitMqListenerContext<Message> context) throws Exception;

    /**
     * 将队列消息 Headers 转化成字符串形式
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    String formatMessageHeadersAsString(HyggeRabbitMqListenerContext<Message> context);

    /**
     * 将队列消息转化成字符串形式
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    String formatMessageBodyAsString(HyggeRabbitMqListenerContext<Message> context);

    /**
     * 将队列消息字符串形式转化成对象
     */
    T formatMessageAsEntity(HyggeRabbitMqListenerContext<Message> context, String messageStringVal) throws Exception;

    /**
     * 消息 headers 覆写，用于日志脱敏
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    default String messageHeadersOverwrite(HyggeRabbitMqListenerContext<Message> context, String headersStringVal, Map<String, Object> headers) {
        return headersStringVal;
    }

    /**
     * 消息 body 覆写，用于日志脱敏
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     *
     * @param messageEntity 转化成对象的
     */
    default String messageBodyOverwrite(HyggeRabbitMqListenerContext<Message> context, String messageStringVal, T messageEntity) {
        return messageStringVal;
    }

    /**
     * 打印接收到的消息信息
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    void printMessageEntityLog(HyggeRabbitMqListenerContext<Message> context, String prefixInfo, String headersStringVal, String messageStringVal);

    /**
     * 收到消息后需要做的业务处理
     */
    void onReceive(HyggeRabbitMqListenerContext<Message> context, T messageEntity) throws Exception;

    /**
     * 根据 {@link HyggeRabbitMqListenerContext#isAutoAckTriggered()} 属性，如果为 true 则自动进行下列 ACK 操作之一：成功消费、消费失败丢弃
     */
    void autoAck(HyggeRabbitMqListenerContext<Message> context, String headersStringVal, String messageStringVal);

    /**
     * 尝试重试行为，返回是否确实执行了重试逻辑
     */
    default boolean retryHook(HyggeRabbitMqListenerContext<Message> context) throws Exception {
        return false;
    }

    /**
     * 业务处理结束时的一些扫尾工作，在自动 ack 执行完成且未执行重试逻辑时触发
     * <p>
     * 下列情况该钩子函数不会被执行：<br/>
     * ① 触发过 requeue 行为
     * ② ack / nack 失败
     * ③ {@link HyggeRabbitMqListenerContext#isBusinessLogicFinishEnable()} 为 false
     */
    default void businessLogicFinishHook(HyggeRabbitMqListenerContext<Message> context) throws Exception {
        // do nothing by default
    }

    /**
     * 链路追踪等技术性组件进行扫尾工作，无论什么情况都会尝试会执行的方法
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    default void finallyHook(HyggeRabbitMqListenerContext<Message> context) {
        // do nothing by default
    }
}
