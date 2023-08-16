package io.github.soupedog.jpa.controller;

import hygge.web.template.HyggeWebUtilContainer;
import hygge.web.template.definition.HyggeController;
import io.github.soupedog.jpa.controller.doc.UserControllerDoc;
import io.github.soupedog.jpa.domain.po.User;
import io.github.soupedog.jpa.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@RestController
public class UserController extends HyggeWebUtilContainer implements UserControllerDoc, HyggeController<ResponseEntity<?>> {
    @Autowired
    private UserServiceImpl userService;

    @Override
    @PostMapping("/user")
    public ResponseEntity<?> saveOrUpdateUser(@RequestBody User user) {
        // PO 和 DTO 用同一个是愚蠢的操作，此处仅用于图方便演示

        // 置空是演示自动更新时间对象，否则会以传入数据落库
        user.setCreateTs(null);
        user.setLastUpdateTs(null);
        return success(userService.saveOrUpdateUser(user));
    }

    @Override
    @GetMapping("/user/{uid}")
    public ResponseEntity<?> queryUser(@PathVariable("uid") Long uid) {
        return success(userService.queryUserByUid(uid));
    }

    @Override
    @GetMapping("/user/custom/multiple")
    public ResponseEntity<?> customQueryUserByPage(@RequestParam(value = "maxId", defaultValue = "100") Long maxId,
                                                   @RequestParam(value = "currentPage", defaultValue = "1") int currentPage,
                                                   @RequestParam(value = "pageSize", defaultValue = "5") int pageSize) {
        return success(userService.customQueryUserByPage(maxId, currentPage, pageSize));
    }

    @Override
    @GetMapping("/user/custom/multiple/hql")
    public ResponseEntity<?> customQueryUserByPageHQL(@RequestParam(value = "maxId", defaultValue = "100") Long maxId,
                                                      @RequestParam(value = "currentPage", defaultValue = "1") int currentPage,
                                                      @RequestParam(value = "pageSize", defaultValue = "5") int pageSize) {
        return success(userService.queryUserListByMaxIdHQL(maxId, currentPage, pageSize));
    }

    @Override
    @PutMapping("/user/custom/increaseCreate")
    public ResponseEntity<?> customIncreaseCreateTs() {
        return success(userService.customIncreaseCreate());
    }
}
