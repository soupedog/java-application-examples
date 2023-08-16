package io.github.soupedog.jpa.controller.doc;

import io.github.soupedog.jpa.domain.po.User;
import io.swagger.v3.oas.annotations.Operation;
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

    ResponseEntity<?> customQueryUserByPage(Long maxId, int currentPage, int pageSize);

    @Operation(summary = "基于 HQL 分页查询", description = "仅查询目标对象 uid/name 值，而 balance 是用数据库随机函数生成")
    ResponseEntity<?> customQueryUserByPageHQL(Long maxId, int currentPage, int pageSize);

    ResponseEntity<?> customIncreaseCreateTs();
}
