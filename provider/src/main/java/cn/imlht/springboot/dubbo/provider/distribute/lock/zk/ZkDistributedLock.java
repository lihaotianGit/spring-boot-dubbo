package cn.imlht.springboot.dubbo.provider.distribute.lock.zk;

import org.apache.zookeeper.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

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

    private int sessionTimeout = 30000;

    /**
     * 配置分布式锁
     * @param lockName 竞争资源
     */
    public ZkDistributedLock(ZooKeeper zk, String lockBase, String lockName) {
        this.zk = zk;
        this.lockBase = "/" + lockBase;
        this.lockName = lockName;
        try {
            // 创建临时有序节点
            currentLock = zk.create(this.lockBase + "/" + this.lockName, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(Thread.currentThread().getName() + " " + currentLock + " 已经创建");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException.NodeExistsException e) {
            System.out.println("NodeExistsException " + Thread.currentThread().getName() + " " + this.lockBase);
        } catch (KeeperException.ConnectionLossException e) {
            System.out.println("ConnectionLossException " + Thread.currentThread().getName() + " " + this.lockBase);
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    // 节点监视器
    public void process(WatchedEvent event) {
        if (this.nodeDeletedLatch != null && event.getType() == Event.EventType.NodeChildrenChanged) {
            nodeDeletedLatch.countDown();
        }
    }

    public void lock() {
        try {
            if (!tryLock()) {
                // 等待锁
                waitForLock(sessionTimeout);
            }
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

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
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean tryLock(long timeout, TimeUnit unit) {
        try {
            return tryLock() || waitForLock(timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 等待锁
    private boolean waitForLock(long waitTime) throws KeeperException, InterruptedException {
        this.nodeDeletedLatch = new CountDownLatch(1);
        this.nodeDeletedLatch.await(waitTime, TimeUnit.MILLISECONDS);
        this.nodeDeletedLatch = null;
        return tryLock(waitTime, TimeUnit.MILLISECONDS);
    }

    public void unlock() {
        try {
            System.out.println("释放锁 " + currentLock);
            this.zk.delete(currentLock, -1);
            currentLock = null;
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    public Condition newCondition() {
        return null;
    }

    public void lockInterruptibly() throws InterruptedException {
        this.lock();
    }
}
