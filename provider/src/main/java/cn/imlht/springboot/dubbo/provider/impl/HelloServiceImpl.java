package cn.imlht.springboot.dubbo.provider.impl;

import cn.imlht.springboot.dubbo.api.HelloService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service("helloService")
public class HelloServiceImpl implements HelloService {

    private final static Logger logger = Logger.getLogger(HelloServiceImpl.class);

    @Override
    public String sayHello() {
        String hello = "Hello Dubbo!";
        logger.info(hello);
        return hello;
    }

}
