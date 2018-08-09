package cn.imlht.springboot.dubbo.provider.distribute.lock.zk.test;

public class VolatileTest {

    private static volatile int race = 0;

    public static void incr() {
        race++;
    }

    private static final int HOLD = 20;

    public static void main(String[] args) {
        Thread[] threads = new Thread[HOLD];
        for (int i = 0; i < HOLD; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++) {
                        incr();
                    }
                }
            });
            threads[i].start();
        }
        while (Thread.activeCount() > 1) {
            Thread.yield();
        }
        System.out.println(race);
    }

}
