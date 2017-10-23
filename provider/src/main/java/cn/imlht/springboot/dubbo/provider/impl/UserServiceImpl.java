package cn.imlht.springboot.dubbo.provider.impl;

import cn.imlht.springboot.dubbo.api.UserService;
import cn.imlht.springboot.dubbo.domain.User;
import cn.imlht.springboot.dubbo.provider.mapper.UserMapper;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service("userService")
public class UserServiceImpl implements UserService {

    private final static Logger logger = Logger.getLogger(UserServiceImpl.class);

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional
    public void insert(User user) {
        int count = userMapper.insert(user);
        logger.info("Inserted user, count: " + count + ", user: " + user.toString());
    }

    @Override
    @Transactional
    public void update(User user) {
        int count = userMapper.update(user);
        logger.info("Updated user, count: " + count + ", user: " + user.toString());
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(long id) {
        User user = userMapper.findById(id);
        if (Objects.isNull(user)) {
            logger.info("Can't find user with id: " + id);
        } else {
            logger.info("Find user, user: " + user.toString());
        }
        return user;
    }

}
