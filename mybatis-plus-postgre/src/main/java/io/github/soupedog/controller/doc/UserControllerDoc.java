package io.github.soupedog.controller.doc;

import io.github.soupedog.domain.po.User;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Map;

/**
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
public interface UserControllerDoc {
    ResponseEntity<?> save(User user);

    // 写得糙了一点，复用了冗余量很大的 User 对象做 swagger 提示
    @RequestBody(
            content = {@Content(schema =
            @Schema(ref = "#/components/schemas/User")
            )}
    )
    ResponseEntity<?> update(Long uid, Map<String, Object> data);

    ResponseEntity<?> queryUser(Long uid);

    ResponseEntity<?> queryUserMultiple(Collection<Long> uidCollection);
}
