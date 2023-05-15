package io.github.soupedog.controller;

import hygge.web.template.HyggeWebUtilContainer;
import hygge.web.template.definition.HyggeController;
import io.github.soupedog.controller.doc.UserControllerDoc;
import io.github.soupedog.domain.po.User;
import io.github.soupedog.domain.po.inner.UserConfiguration;
import io.github.soupedog.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

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
    public ResponseEntity<?> save(@RequestBody User user) {
        return success(userService.saveUser(user));
    }

    @Override
    @PutMapping("/user/{uid}")
    public ResponseEntity<?> update(@PathVariable("uid") Long uid, @RequestBody Map<String, Object> data) {
        Object configurationTemp = data.get("configuration");
        if (configurationTemp != null) {
            // 需要转换成对应类型才会激活对应 TypeHandler
            UserConfiguration configuration = jsonHelper.readAsObject(jsonHelper.formatAsString(configurationTemp), UserConfiguration.class);
            data.put("configuration", configuration);
        }

        return success(userService.updateUser(uid, data, new Timestamp(System.currentTimeMillis())));
    }

    @Override
    @GetMapping("/user/{uid}")
    public ResponseEntity<?> queryUser(@PathVariable("uid") Long uid) {
        return success(userService.queryUserByUid(uid));
    }

    @Override
    @GetMapping("/user/multiple")
    public ResponseEntity<?> queryUserMultiple(@RequestParam("uid") Collection<Long> uidCollection) {
        return success(userService.queryUserByUid(uidCollection));
    }
}
