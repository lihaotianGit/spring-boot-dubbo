package cn.imlht.springboot.dubbo.consumer.endpoint;

import cn.imlht.springboot.dubbo.api.UserService;
import cn.imlht.springboot.dubbo.domain.User;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/user")
public class UserResource {

    private final static Logger logger = Logger.getLogger(UserResource.class);

    @Resource
    private UserService userService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public User findById(@PathVariable long id) {
        logger.info("Id: " + id);
        User user = userService.findById(id);
        if (Objects.isNull(user)) {
            return null;
        } else {
            logger.info("User: " + user.toString());
            return user;
        }
    }

}
