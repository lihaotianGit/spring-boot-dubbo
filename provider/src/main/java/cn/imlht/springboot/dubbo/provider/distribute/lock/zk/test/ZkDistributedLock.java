package cn.imlht.springboot.dubbo.provider.distribute.lock.zk.test;

import cn.imlht.springboot.dubbo.provider.exception.OperationNotSupportedException;
import org.apache.zookeeper.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * ZooKeeper 分布式锁
 * 0. 创建持久性根节点，分布式锁为根节点下的临时有序子节点。
 * 1. 获取锁：创建子节点并将所有子节点排序，创建最小节点的线程视为持有锁。
 * 2. 等锁：子节点无变化则持续等锁，到过期时间为止。
 * 3. 锁续期：不需要。
 * 4. 解锁：删除目前最小子节点。
 */
public class ZkDistributedLock implements Lock, Watcher {

    private ZooKeeper zk;
    // 根节点
    private String lockBase;
    // 竞争的资源，子节点名称前缀
    private String lockName;
    // 当前锁
    private String currentLock;
    // 计数器
    private CountDownLatch nodeDeletedLatch;
    private static final byte[] data = new byte[0];

    private int sessionTimeout = 30000;

    public ZkDistributedLock(ZooKeeper zk, String lockBase, String lockName) {
        this.zk = zk;
        this.lockName = lockName;
        this.lockBase = "/" + lockBase;
        try {
            if (Objects.isNull(zk.exists(this.lockBase, false))) {
                synchronized (this) {
                    // 如果根节点不存在，则创建根节点
                    if (Objects.isNull(zk.exists(this.lockBase, false))) {
                        zk.create(this.lockBase, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    }
                }
            }
        } catch (KeeperException.NodeExistsException e) {
            System.out.println("NodeExistsException " + Thread.currentThread().getName() + " " + this.lockBase);
        } catch (KeeperException.ConnectionLossException e) {
            System.out.println("ConnectionLossException " + Thread.currentThread().getName() + " " + this.lockBase);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException();
        }
        createSubNode();
    }

    private void createSubNode() {
        // 创建临时有序子节点
        try {
            currentLock = zk.create(this.lockBase + "/" + this.lockName, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException();
        }
        System.out.println(Thread.currentThread().getName() + " " + currentLock + " 已经创建");
    }

    @Override
    public void process(WatchedEvent event) {
        if (this.nodeDeletedLatch != null && event.getType() == Event.EventType.NodeChildrenChanged) {
            nodeDeletedLatch.countDown();
        }
    }

    @Override
    public void lock() {
        try {
            if (!tryLock()) {
                waitForLock(sessionTimeout, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {

        } catch (KeeperException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean tryLock() {
        try {
            // 取所有子节点
            List<String> subNodes = zk.getChildren(this.lockBase, this);

            Collections.sort(subNodes);
            // 若当前节点为最小节点，则获取锁成功
            if (currentLock.equals(this.lockBase + "/" + subNodes.get(0))) {
                System.out.println(Thread.currentThread().getName() + " 获得了锁" + " " + currentLock);
                return true;
            }
        } catch (InterruptedException e) {
            unlock();
        } catch (KeeperException e) {
            throw new RuntimeException();
        }
        return false;
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            return tryLock() || waitForLock(timeout, unit);
        } catch (InterruptedException e) {
            throw new InterruptedException();
        } catch (KeeperException e) {
            throw new RuntimeException();
        }
    }

    // 等待锁
    private boolean waitForLock(long waitTime, TimeUnit unit) throws KeeperException, InterruptedException {
        this.nodeDeletedLatch = new CountDownLatch(1);
        try {
            this.nodeDeletedLatch.await(waitTime, unit);
        } catch (InterruptedException e) {
            unlock();
            throw new InterruptedException();
        } finally {
            this.nodeDeletedLatch = null;
        }
        return tryLock(waitTime, unit);
    }

    @Override
    public void unlock() {
        try {
            System.out.println("释放锁 " + currentLock);
            this.zk.delete(currentLock, -1);
            currentLock = null;
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Condition newCondition() {
        throw new OperationNotSupportedException("Method newCondition() not supported.");
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.lock();
    }
}
