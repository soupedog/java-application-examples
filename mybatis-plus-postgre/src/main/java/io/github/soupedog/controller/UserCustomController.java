package io.github.soupedog.controller;

import hygge.web.template.HyggeWebUtilContainer;
import hygge.web.template.definition.HyggeController;
import io.github.soupedog.controller.doc.UserCustomControllerDoc;
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
 * @date 2023/6/09
 * @since 1.0
 */
@RestController
public class UserCustomController extends HyggeWebUtilContainer implements UserCustomControllerDoc, HyggeController<ResponseEntity<?>> {
    @Autowired
    private UserServiceImpl userService;

    @Override
    @PostMapping("/user/custom")
    public ResponseEntity<?> customSaveUser(@RequestBody User user) {
        // 这是一个愚蠢的操作，PO 和 DTO 用同一个，此处仅用于图方便演示
        user.setUid(null);
        user.setSequence(null);
        return success(userService.customSaveUser(user));
    }

    @Override
    @PutMapping("/user/custom/{uid}")
    public ResponseEntity<?> customUpdateUser(@PathVariable("uid") Long uid, @RequestBody Map<String, Object> data) {
        Object configurationTemp = data.get("configuration");
        if (configurationTemp != null) {
            // 需要转换成对应类型才会激活对应 TypeHandler
            UserConfiguration configuration = jsonHelper.readAsObject(jsonHelper.formatAsString(configurationTemp), UserConfiguration.class);
            data.put("configuration", configuration);
        }

        return success(userService.customUpdateUser(uid, data, new Timestamp(System.currentTimeMillis())));
    }

    @Override
    @GetMapping("/user/custom/multiple")
    public ResponseEntity<?> customQueryUserMultiple(@RequestParam("uid") Collection<Long> uidCollection) {
        return success(userService.customQueryUserMultiple(uidCollection));
    }
}
