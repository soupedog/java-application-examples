package io.github.soupedog.mybatis.controller.doc;

import io.github.soupedog.mybatis.domain.po.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
@Tag(name = "UserController", description = "基于 Mybatis Plus 提供的语法糖实现")
public interface UserControllerDoc {
    ResponseEntity<?> saveOrUpdateUser(User user);

    ResponseEntity<?> queryUser(Long uid);
}
