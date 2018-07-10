package cn.imlht.springboot.dubbo.provider.distribute.lock.redis;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

public class ExtendThread extends Thread {

    private final static Logger logger = Logger.getLogger(ExtendThread.class);

    private Jedis jedis;
    private String uuid;
    private String lockName;
    private long lockExpiredMilliseconds;

    ExtendThread(Jedis jedis, String lockName, String uuid, long lockExpiredMilliseconds) {
        this.uuid = uuid;
        this.jedis = jedis;
        this.lockName = lockName;
        this.lockExpiredMilliseconds = lockExpiredMilliseconds;
    }

    /**
     * 锁续期，解锁后会中断该线程
     */
    @Override
    public void run() {
        try {
            int lockExpiredSeconds = Math.toIntExact(TimeUnit.MILLISECONDS.toSeconds(lockExpiredMilliseconds));
            lockExpiredSeconds = lockExpiredSeconds == 0 ? 1 : lockExpiredSeconds;
            // 删除锁之后，会中断延时线程，在此处需要响应中断
            while (!this.isInterrupted() && jedis.isConnected()) {
                try {
                    Thread.sleep(lockExpiredMilliseconds / 10);
                } catch (InterruptedException e) {
                    logger.info("Extend Thread Sleep Interrupted");
                    return;
                }
                if (jedis.isConnected() && Long.compare(1L, jedis.expire(lockName, lockExpiredSeconds)) == 0) {
                    logger.info(printInfo("Extend Success"));
                } else {
                    logger.info(printInfo("Extend Failed"));
                    return;
                }
            }
        } finally {
            // 锁续期线程结束前归还连接到连接池
            jedis.close();
        }
    }

    private String printInfo(String prefix) {
        return prefix + ": " + lockName + " " + Thread.currentThread().getName() + " " + uuid;
    }
}
