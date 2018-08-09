package cn.imlht.springboot.dubbo.provider.distribute.lock.zk.test;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Demo2 implements Lock, Watcher {
    private final static Logger logger = Logger.getLogger(Demo2.class);

    private static final String SPLITSTR = "_lock_";
    private static final int SESSION_TIMEOUT = 60000;//等锁的毫秒数
    private static final byte[] data = new byte[0];


    private ZooKeeper zk = null;

    private String root = "/locks";//根
    private String lockName;//竞争资源的标志
    private String waitNode;//等待前一个锁
    private String myZnode;//当前锁

    private CountDownLatch latch;//计数器

    /**
     * 创建分布式锁,使用前请确认config配置的zookeeper服务可用
     * @param server 127.0.0.1:2181
     * @param lockName 竞争资源标志,lockName中不能包含单词lock
     */
    public Demo2(String server, String lockName){
        this.lockName = lockName;
        // 创建一个与服务器的连接
        try {
            zk = initZk(server);
            Stat stat = zk.exists(root, false);
            if(stat == null){
                // 创建根节点
                zk.create(root, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            throw new LockException(e);
        }
    }

    /**
     * zookeeper节点的监视器
     */
    @Override
    public void process(WatchedEvent event) {
        if(this.latch != null) {
            this.latch.countDown();
        }
    }

    @Override
    public void lock() {
        try {
            if(!tryLock()){
                boolean locked = waitForLock(waitNode, SESSION_TIMEOUT, TimeUnit.MILLISECONDS);//等待锁
                if(!locked){
                    logger.error("can not lock...");
                }
            }
        } catch (Exception e) {
            throw new LockException(e);
        }
    }

    public boolean tryLock() {
        try {
            if(lockName.contains(SPLITSTR)){
                throw new LockException("lockName can not contains \\u000B");
            }
            //创建临时子节点
            myZnode = zk.create(root + "/" + lockName + SPLITSTR, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);

            //取出所有子节点
            List<String> subNodes = zk.getChildren(root, false);
            //取出所有lockName的锁
            List<String> lockObjNodes = new ArrayList<>();
            for (String node : subNodes) {
                String _node = node.split(SPLITSTR)[0];
                if(_node.equals(lockName)){
                    lockObjNodes.add(node);
                }
            }
            Collections.sort(lockObjNodes);

            if(myZnode.equals(root+"/"+lockObjNodes.get(0))){
                //如果是最小的节点,则表示取得锁
                return true;
            }
            //如果不是最小的节点，找到比自己小1的节点
            String subMyZnode = myZnode.substring(myZnode.lastIndexOf("/") + 1);
            waitNode = lockObjNodes.get(Collections.binarySearch(lockObjNodes, subMyZnode) - 1);
        } catch (Exception e) {
            throw new LockException(e);
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        try {
            return tryLock() || waitForLock(waitNode, time, unit);
        } catch (Exception e) {
            throw new LockException(e);
        }
    }

    private boolean waitForLock(String lower, long waitTime, TimeUnit unit) throws InterruptedException, KeeperException {
        Stat stat = zk.exists(root + "/" + lower, true);
        //判断比自己小一个数的节点是否存在,如果不存在则无需等待锁,同时注册监听
        if(stat != null){
            this.latch = new CountDownLatch(1);
            this.latch.await(waitTime, unit);
            this.latch = null;
        }
        return true;
    }

    @Override
    public void unlock() {
        try {
            zk.delete(myZnode,-1);
            myZnode = null;
        } catch (Exception e) {
            throw new LockException(e);
        }
    }

    private synchronized ZooKeeper initZk(String server) {
        try {
            if(zk == null){
                zk = new ZooKeeper(server, SESSION_TIMEOUT, this);
            }
        } catch (IOException e) {
            throw new LockException("zk init connect fail" + e.getMessage());
        }
        return zk;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.lock();
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    private class LockException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private LockException(String e){
            super(e);
        }
        private LockException(Exception e){
            super(e);
        }
    }
}
