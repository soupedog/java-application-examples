package io.github.soupedog.rabbitmq.service.listener.base.definition;

import hygge.commons.exception.ParameterRuntimeException;
import hygge.util.UtilCreator;
import hygge.util.definition.CollectionHelper;
import hygge.util.definition.JsonHelper;
import hygge.util.definition.ParameterHelper;
import hygge.util.definition.RandomHelper;
import io.github.soupedog.rabbitmq.service.listener.base.HyggeRabbitMQMessageItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.boot.logging.LogLevel;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public interface HyggeListenerBaseFeature {
    Logger log = LoggerFactory.getLogger(HyggeListenerBaseFeature.class);
    JsonHelper<?> jsonHelper = UtilCreator.INSTANCE.getDefaultJsonHelperInstance(false);
    ParameterHelper parameterHelper = UtilCreator.INSTANCE.getDefaultInstance(ParameterHelper.class);
    CollectionHelper collectionHelper = UtilCreator.INSTANCE.getDefaultInstance(CollectionHelper.class);
    RandomHelper randomHelper = UtilCreator.INSTANCE.getDefaultInstance(RandomHelper.class);
    String DEFAULT_HEADERS_KEY_ENVIRONMENT_NAME = "hygge-environment-name";
    String DEFAULT_HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER = "hygge-requeue-counter";

    /**
     * 获取 Listener 名称(用于日志输出)
     */
    String getListenerName();

    default String getValueFromHeaders(HyggeRabbitMQMessageItem<?> messageItem, String key, boolean nullable) {
        String result = getValueFromHeaders(messageItem.getMessage(), key);
        if (!nullable && !StringUtils.hasText(result)) {
            messageItem.setException(new ParameterRuntimeException(getListenerName() + " fail to get [" + key + "] from headers of rabbitmq message, it can't be empty."));
        }
        return result;
    }

    default String getValueFromHeaders(Message message, String key) {
        return Optional.ofNullable(message)
                .map(Message::getMessageProperties)
                .map(messageProperties -> (String) messageProperties.getHeader(key))
                .orElse(null);
    }

    default void printLog(LogLevel logLevel, String logInfo, Throwable throwable) {
        switch (logLevel) {
            case TRACE:
                log.trace(logInfo, throwable);
                break;
            case DEBUG:
                log.debug(logInfo, throwable);
                break;
            case INFO:
                log.info(logInfo, throwable);
                break;
            case WARN:
                log.warn(logInfo, throwable);
                break;
            default:
                log.error(logInfo, throwable);
                break;
        }
    }
}
