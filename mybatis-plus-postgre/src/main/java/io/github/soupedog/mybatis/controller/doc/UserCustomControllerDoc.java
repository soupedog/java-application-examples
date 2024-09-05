package io.github.soupedog.mybatis.controller.doc;

import io.github.soupedog.mybatis.domain.po.User;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Map;

/**
 * @author Xavier
 * @date 2023/6/09
 * @since 1.0
 */
@Tag(name = "UserCustomController", description = "基于 Mybatis 自定义 XML 配置实现")
public interface UserCustomControllerDoc {
    ResponseEntity<?> customSaveUser(User user);

    // 写得糙了一点，复用了冗余量很大的 User 对象做 swagger 提示
    @RequestBody(
            content = {@Content(schema =
            @Schema(ref = "#/components/schemas/User")
            )}
    )
    ResponseEntity<?> customUpdateUser(Long uid, Map<String, Object> data);

    ResponseEntity<?> customQueryUserSingle(Long uid);

    ResponseEntity<?> customQueryUserMultiple(Collection<Long> uidCollection);
}
