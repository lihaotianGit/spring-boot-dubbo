package cn.imlht.springboot.dubbo.provider.distribute.lock.redis;

import cn.imlht.springboot.dubbo.provider.exception.OperationNotSupportedException;
import cn.imlht.springboot.dubbo.provider.exception.UnmatchedLockException;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static java.util.Collections.singletonList;

/**
 * Redis 分布式锁
 * 1. 获取锁：使用单条set nx px 语句，保证原子性；
 *      nx：键必须不存在才会设置成功，结合Redis单线程执行的特性，同一时间内只能有单个线程设置成功，px：过期时间。
 * 2. 等锁：每隔n毫秒尝试获取一次锁，会响应中断。
 * 3. 锁续期：新起单一线程给Redis发送续期指令，解锁后会中断该线程。
 * 4. 解锁：使用Lua脚本解锁，保证原子性；对比Redis key中的value是否为期望值，如是则视为锁为该线程拥有，可以解锁；如不是则抛出异常。
 */
public class RedisDistributedLock implements Lock {

    private final static Logger logger = Logger.getLogger(RedisDistributedLock.class);

    private Jedis jedis;
    private String uuid;
    private String lockName;
    private long lockExpiredMilliseconds;

    private final static String LOCKED = "OK";
    private final static String UNLOCKED = "1";
    private final static int BLOCK_MILLISECONDS = 100;
    private final static String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private LockExtendThread lockExtendThread;

    private boolean locked = false;

    public RedisDistributedLock(Jedis jedis, String lockName, long lockExpiredMilliseconds) {
        this.jedis = jedis;
        this.lockName = lockName;
        this.uuid = UUID.randomUUID().toString();
        this.lockExpiredMilliseconds = lockExpiredMilliseconds;
        this.lockExtendThread = new LockExtendThread(jedis, lockName, uuid, lockExpiredMilliseconds);
    }

    /**
     * 阻塞等锁，忽略中断；
     */
    @Override
    public void lock() {
        try {
            this.tryLock(0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Lock for InterruptedException, Ignored.");
        }
    }

    /**
     * 阻塞等锁，响应中断;
     * 如果已拿到锁，则会放弃锁；如果未拿到锁，则会退出锁竞争
     * @throws InterruptedException 等锁时线程被中断，抛出该异常
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.tryLock(0, TimeUnit.MILLISECONDS);
    }

    /**
     * 尝试获取锁，无论结果如何，立即返回
     * @return true，获取锁；false，未获取锁
     */
    @Override
    public boolean tryLock() {
        if (LOCKED.equals(jedis.set(lockName, uuid, "NX", "PX", lockExpiredMilliseconds))) {
            extendLock();
            logger.info(printInfo("Locked"));
            locked = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 锁续期
     */
    private void extendLock() {
        lockExtendThread.start();
    }

    /**
     * 在指定时间内尝试获取锁并响应中断；
     * 如果已拿到锁，则会放弃锁；如果未拿到锁，则会退出锁竞争
     * @param time 如果为0，则无限制等锁
     * @param unit time的时间单位
     * @return true，如果在指定时间内获取到锁；false，time为大于0的情况下，超过指定时间仍未获取到锁
     * @throws InterruptedException 响应中断
     */
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        try {
            return this.tryLockPrivately(time, unit);
        } catch (InterruptedException e) {
            if (locked) this.unlock();
            jedis.close();
            throw new InterruptedException();
        }
    }

    private boolean tryLockPrivately(long time, TimeUnit unit) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        long start = System.currentTimeMillis();
        for (; ; ) {
            if (this.tryLock()) {
                return true;
            } else {
                if (Long.compare(time, 0L) != 0 && Long.compare(System.currentTimeMillis() - start, unit.toMillis(time)) >= 0) {
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

    /**
     * 解锁：使用Lua脚本，保证操作的原子性
     * @throws UnmatchedLockException 如果解锁时发现锁被其他线程持有，则抛出该异常
     */
    @Override
    public void unlock() {
        if (UNLOCKED.equals(String.valueOf(jedis.eval(UNLOCK_SCRIPT, singletonList(lockName), singletonList(uuid))))) {
            logger.info(printInfo("Unlocked"));
        } else {
            logger.error(printInfo("Unlock Failed"));
            throw new UnmatchedLockException(printInfo("Unmatched lock") + " redis lock value: " + jedis.get(lockName));
        }
        locked = false;
        // 中断锁续期线程
        lockExtendThread.interrupt();
    }

    @Override
    public Condition newCondition() {
        throw new OperationNotSupportedException("Method newCondition() not supported.");
    }

    private String printInfo(String prefix) {
        return prefix + ": " + lockName + " " + Thread.currentThread().getName() + " " + uuid;
    }

}
