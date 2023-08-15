package io.github.soupedog.jpa.service;

import io.github.soupedog.jpa.domain.po.User;
import io.github.soupedog.jpa.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

/**
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@Service
public class UserServiceImpl {
    @Autowired
    private UserDao userDao;

    public User saveOrUpdateUser(User user) {
        return userDao.save(user);
    }

    public User queryUserByUid(Long uid) {
        // example 赋什么属性查询就要求什么属性
        Example<User> example = Example.of(User.builder().uid(uid).build());

        // 等效 "userDao.findById(uid);"
        return userDao.findOne(example).orElse(null);
    }

    public int customIncreaseCreate() {
        return userDao.plus3MillisecondForCreateTs();
    }
}
