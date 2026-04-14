package io.github.soupedog.kafka.domian;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Xavier
 * @date 2026/4/14
 */
public interface HyggeKafkaMessageEntity {
    /**
     * 获取唯一标识
     */
    @JsonIgnore
    <T> T getUniqueIdentification();

    /**
     * 获取消息对象
     */
    @JsonIgnore
    <T> T getMessageEntity();
}
