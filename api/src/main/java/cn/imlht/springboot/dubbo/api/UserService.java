package cn.imlht.springboot.dubbo.api;

import cn.imlht.springboot.dubbo.domain.User;

public interface UserService {

    void insert(User user);

    void update(User user);

    User findById(long id);

}
