package cn.imlht.springboot.dubbo.provider;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {

    private final static Logger logger = Logger.getLogger(RedisTest.class);

    @Resource
    private RedisTemplate redisTemplate;

    @Resource(name="redisTemplate")
    private ValueOperations<String, Object> valueOperations;

    @Test
    public void should_store() {
        valueOperations.set("test_name", "Jon", 100, TimeUnit.SECONDS);
        logger.info(String.valueOf(valueOperations.get("test_name")));
    }
}
