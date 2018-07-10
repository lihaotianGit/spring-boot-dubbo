package cn.imlht.springboot.dubbo.provider;

import cn.imlht.springboot.dubbo.provider.distribute.lock.zk.test.Count;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {

    private final static Logger logger = Logger.getLogger(RedisTest.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name="redisTemplate")
    private ValueOperations<String, Object> valueOperations;

    @Resource
    private JedisPool jedisPool;

    @Test
    public void should_store() {
        valueOperations.set("test_name", "Jon", 100, TimeUnit.SECONDS);
        logger.info(String.valueOf(valueOperations.get("test_name")));

        final String uuid = UUID.randomUUID().toString();
        final String lockKey = "lock";

        String setLockResult = redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                Jedis jedis = (Jedis) connection.getNativeConnection();
                return jedis.set(lockKey, uuid, "NX", "PX", 20000);
            }
        });
        System.out.println(setLockResult);

        Object delLockResult = redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                Jedis jedis = (Jedis) connection.getNativeConnection();
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                return jedis.eval(script, singletonList(lockKey), singletonList(uuid));
            }
        });

        System.out.println(String.valueOf(delLockResult));
    }

    @Test
    public void should_distributed_lock() throws InterruptedException {
        Count count = new Count();

        ExecutorService executorService = Executors.newFixedThreadPool(80);

        List<RedisThread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threads.add(new RedisThread(count, jedisPool.getResource(), 50));
        }

        for (Thread thread: threads) {
            executorService.execute(thread);
        }
        executorService.shutdown();

        while (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS));
        System.out.println(count.get());
    }
}
