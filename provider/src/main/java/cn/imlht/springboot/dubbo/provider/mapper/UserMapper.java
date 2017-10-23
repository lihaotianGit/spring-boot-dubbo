package cn.imlht.springboot.dubbo.provider.mapper;

import cn.imlht.springboot.dubbo.domain.User;

public interface UserMapper {

    int insert(User user);

    int update(User user);

    User findById(long id);
}
