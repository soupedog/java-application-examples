package io.github.soupedog.kafka.domian;

import hygge.commons.template.definition.DeepCloneable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@Data
@Schema
public class User implements HyggeKafkaMessageEntity, DeepCloneable<User> {
    @Schema(description = "用户唯一标识", example = "001")
    private String uid;
    @Schema(description = "用户名称", example = "张三")
    private String name;
    @Schema(description = "用户年龄", example = "18")
    private Integer age;

    @Override
    public <T> T getUniqueIdentification() {
        return (T) uid;
    }

    @Override
    public <T> T getMessageEntity() {
        return (T) this;
    }
}
