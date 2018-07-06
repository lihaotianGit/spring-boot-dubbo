package cn.imlht.springboot.dubbo.provider.distribute.lock.test;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LockTest {

    private Count count = new Count();



    private void test() throws InterruptedException {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper("127.0.0.1:2181", 300000, event -> System.out.println("WatchedEvent in main"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(80);

        List<MyThread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threads.add(new MyThread(count, zk));
        }

        for (Thread thread: threads) {
            executorService.execute(thread);
        }
        executorService.shutdown();

        while (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS));
        zk.close();
        zk = null;
        System.out.println(count.get());

    }

    public static void main(String[] args) throws InterruptedException {

        new LockTest().test();






//        for (int i = 0; i < 10; i++) {
//            Thread t = new Thread(runnable);
//            t.start();
//        }
    }
}
