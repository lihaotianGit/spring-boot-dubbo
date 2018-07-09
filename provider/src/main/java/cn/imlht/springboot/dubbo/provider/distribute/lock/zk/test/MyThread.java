package cn.imlht.springboot.dubbo.provider.distribute.lock.zk.test;

import cn.imlht.springboot.dubbo.provider.distribute.lock.zk.ZkDistributedLock;
import org.apache.zookeeper.ZooKeeper;

public class MyThread extends Thread {

    private Count count;
    private ZooKeeper zk;

    MyThread(Count count, ZooKeeper zk) {
        this.count = count;
        this.zk = zk;
    }

    @Override
    public void run() {
        ZkDistributedLock lock = new ZkDistributedLock(zk, "locks0", "test3");
        lock.lock();
        for (int i = 0; i < 100; i++) {
            count.incr();
        }
        System.out.println(Thread.currentThread().getName() + " 正在运行 count " + count.get());
        lock.unlock();
    }
}
