package cn.bugstack.exception;

//异常基类
public class DomainException extends RuntimeException {

    protected DomainException(String message){
        super(message);
    }

    protected DomainException(String message, Throwable cause){
        super(message, cause);
    }
}
