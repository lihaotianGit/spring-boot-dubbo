package cn.imlht.springboot.dubbo.provider.exception;

public class WaitLockTimedOutException extends RuntimeException {

    public WaitLockTimedOutException(String message) {
        super(message);
    }

}
