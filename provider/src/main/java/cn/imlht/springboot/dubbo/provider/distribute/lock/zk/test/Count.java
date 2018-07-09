package cn.imlht.springboot.dubbo.provider.distribute.lock.zk.test;

public class Count {

    private int i;

    public void incr() {
        i = i + 1;
    }

    public int get() {
        return i;
    }

    public void set(int j) {
        this.i = j;
    }

}
