package cn.imlht.springboot.dubbo.provider.exception;

public class UnmatchedLockException extends RuntimeException {

    public UnmatchedLockException(String message) {
        super(message);
    }
}
