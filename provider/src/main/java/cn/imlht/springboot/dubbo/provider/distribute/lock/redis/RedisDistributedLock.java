package cn.imlht.springboot.dubbo.provider.distribute.lock.redis;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static java.util.Collections.singletonList;

public class RedisDistributedLock implements Lock {

    private final static Logger logger = Logger.getLogger(RedisDistributedLock.class);

    private String uuid;
    private String lockName;
    private long expiredMilliseconds;
    private RedisTemplate<String, Object> redisTemplate;

    private final static String LOCKED = "OK";
    private final static String UNLOCKED = "1";
    private final static int BLOCK_MILLISECONDS = 100;
    private final static String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    public RedisDistributedLock(RedisTemplate<String, Object> redisTemplate, String lockName, long expiredMilliseconds) {
        this.lockName = lockName;
        this.redisTemplate = redisTemplate;
        this.uuid = UUID.randomUUID().toString();
        this.expiredMilliseconds = expiredMilliseconds;
    }

    @Override
    public void lock() {
        try {
            logger.info(printInfo("Info"));
            this.tryLock(expiredMilliseconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Lock for InterruptedException, Ignored.");
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        String lockResult = redisTemplate.execute((RedisCallback<String>) connection ->
                ((Jedis) connection.getNativeConnection()).set(lockName, uuid, "NX", "PX", expiredMilliseconds));
        if (LOCKED.equals(lockResult)) {
            logger.info(printInfo("Locked"));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        long start = System.currentTimeMillis();
        for (; ; ) {
            if (this.tryLock()) {
                return true;
            } else {
                long now = System.currentTimeMillis();
                if (Long.compare(now - start, unit.toMillis(time)) >= 0) {
                    return false;
                } else {
                    logger.info(printInfo("Wait"));
                    Thread.sleep(BLOCK_MILLISECONDS);
                }
            }
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
    }

    @Override
    public void unlock() {
        Object delLockResult = redisTemplate.execute((RedisCallback<Object>) connection ->
                ((Jedis) connection.getNativeConnection()).eval(UNLOCK_SCRIPT, singletonList(lockName), singletonList(uuid)));
        if (UNLOCKED.equals(String.valueOf(delLockResult))) {
            logger.info(printInfo("Unlocked"));
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    private String printInfo(String prefix) {
        return prefix + ": " + lockName + " " + Thread.currentThread().getName() + " " + uuid;
    }

}
