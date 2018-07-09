package cn.imlht.springboot.dubbo.provider;

import cn.imlht.springboot.dubbo.provider.distribute.lock.redis.RedisDistributedLock;
import cn.imlht.springboot.dubbo.provider.distribute.lock.zk.test.Count;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisThread extends Thread {

    private Count count;
    RedisTemplate<String, Object> redisTemplate;

    RedisThread(Count count, RedisTemplate<String, Object> redisTemplate) {
        this.count = count;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run() {
        RedisDistributedLock lock = new RedisDistributedLock(redisTemplate, "lock", 2000);
        lock.lock();
        for (int i = 0; i < 100; i++) {
            count.incr();
        }
        System.out.println(Thread.currentThread().getName() + " 正在运行 count " + count.get());
        lock.unlock();
    }
}
