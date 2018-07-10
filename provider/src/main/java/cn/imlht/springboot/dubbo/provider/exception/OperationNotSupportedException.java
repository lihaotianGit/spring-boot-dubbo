package cn.imlht.springboot.dubbo.provider.exception;

public class OperationNotSupportedException extends RuntimeException {

    public OperationNotSupportedException(String message) {
        super(message);
    }
}
