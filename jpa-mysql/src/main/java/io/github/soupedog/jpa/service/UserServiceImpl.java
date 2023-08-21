package io.github.soupedog.jpa.service;

import io.github.soupedog.jpa.domain.po.User;
import io.github.soupedog.jpa.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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

    @Transactional(Transactional.TxType.NEVER)
    public User queryUserByUid(Long uid) {
        // example 赋什么属性查询就要求什么属性
        Example<User> example = Example.of(User.builder().uid(uid).build());

        // 等效 "userDao.findById(uid);"
        return userDao.findOne(example).orElse(null);
    }

    public Page<User> customQueryUserByPage(Long maxId, int currentPage, int pageSize) {
        Sort sort = Sort.by(Sort.Order.desc("createTs"));
        // 0 代表第一页，currentPage 是人理解的页码
        PageRequest pageRequest = PageRequest.of(currentPage - 1, pageSize, sort);
        return userDao.queryUserListByMaxIdNative(maxId, pageRequest);
    }

    public Page<User> queryUserListByMaxIdHQL(Long maxId, int currentPage, int pageSize) {
        Sort sort = Sort.by(Sort.Order.desc("createTs"));
        // 0 代表第一页，currentPage 是人理解的页码
        PageRequest pageRequest = PageRequest.of(currentPage - 1, pageSize, sort);
        return userDao.queryUserListByMaxIdHQL(maxId, pageRequest);
    }

    public int customIncreaseCreate() {
        return userDao.plus3MillisecondForCreateTs();
    }
}
