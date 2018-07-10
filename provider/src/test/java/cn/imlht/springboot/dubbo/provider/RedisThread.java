package cn.imlht.springboot.dubbo.provider;

import cn.imlht.springboot.dubbo.provider.distribute.lock.redis.RedisDistributedLock;
import cn.imlht.springboot.dubbo.provider.distribute.lock.zk.test.Count;
import redis.clients.jedis.Jedis;

public class RedisThread extends Thread {

    private Count count;
    private Jedis jedis;
    private long lockExpired;

    RedisThread(Count count, Jedis jedis, long lockExpired) {
        this.count = count;
        this.jedis = jedis;
        this.lockExpired = lockExpired;
    }

    @Override
    public void run() {
        RedisDistributedLock lock = new RedisDistributedLock(jedis, "lock", lockExpired);
        lock.lock();
        for (int i = 0; i < 100; i++) {
            count.incr();
        }
        lock.unlock();
    }
}
