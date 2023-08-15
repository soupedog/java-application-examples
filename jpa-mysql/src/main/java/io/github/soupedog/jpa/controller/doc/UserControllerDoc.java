package io.github.soupedog.jpa.controller.doc;

import io.github.soupedog.jpa.domain.po.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@Tag(name = "UserController", description = "基于 Spring-Data-Jpa 提供的语法糖实现")
public interface UserControllerDoc {
    ResponseEntity<?> saveOrUpdateUser(User user);

    ResponseEntity<?> queryUser(Long uid);

    ResponseEntity<?> customIncreaseCreateTs();
}
