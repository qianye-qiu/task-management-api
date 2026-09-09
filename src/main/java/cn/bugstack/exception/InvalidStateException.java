package cn.bugstack.exception;

//非法状态异常
public class InvalidStateException extends DomainException {

    public InvalidStateException(String message){
        super(message);
    }
}
