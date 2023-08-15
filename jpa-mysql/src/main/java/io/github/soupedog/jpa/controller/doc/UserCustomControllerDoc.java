package io.github.soupedog.jpa.controller.doc;

import io.github.soupedog.jpa.domain.po.User;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Map;

/**
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@Tag(name = "UserCustomController", description = "基于 Jpa 自定义 HQL/原生 SQL 实现")
public interface UserCustomControllerDoc {
    ResponseEntity<?> customSaveUser(User user);

    // 写得糙了一点，复用了冗余量很大的 User 对象做 swagger 提示
    @RequestBody(
            content = {@Content(schema =
            @Schema(ref = "#/components/schemas/User")
            )}
    )
    ResponseEntity<?> customUpdateUser(Long uid, Map<String, Object> data);

    ResponseEntity<?> customQueryUserMultiple(Collection<Long> uidCollection);
}
