package io.github.soupedog.controller;

import hygge.web.template.HyggeWebUtilContainer;
import hygge.web.template.definition.HyggeController;
import io.github.soupedog.controller.doc.UserControllerDoc;
import io.github.soupedog.domain.po.User;
import io.github.soupedog.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
@RestController
public class UserController extends HyggeWebUtilContainer implements UserControllerDoc, HyggeController<ResponseEntity<?>> {
    @Autowired
    private UserServiceImpl userService;

    @Override
    @PostMapping("/user")
    public ResponseEntity<?> saveOrUpdateUser(@RequestBody User user) {
        // 这是一个愚蠢的操作，PO 和 DTO 用同一个，此处仅用于图方便演示，做一些参数清理
        user.setUid(null);
        user.setSequence(null);
        user.setCreateTs(null);
        user.setLastUpdateTs(null);
        return success(userService.saveOrUpdateUser(user));
    }

    @Override
    @GetMapping("/user/{uid}")
    public ResponseEntity<?> queryUser(@PathVariable("uid") Long uid) {
        return success(userService.queryUserByUid(uid));
    }
}
