package cn.imlht.springboot.dubbo.provider.exception;

public class ResourceCannotBeClosedException extends RuntimeException {

    public ResourceCannotBeClosedException(String message) {
        super(message);
    }
}
