package cn.imlht.springboot.dubbo.consumer.endpoint;

import cn.imlht.springboot.dubbo.api.HelloService;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class HelloResource {

    private final static Logger logger = Logger.getLogger(HelloResource.class);

    @Resource
    private HelloService helloService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        logger.info("In consumer ...");
        return helloService.sayHello();
    }
}
