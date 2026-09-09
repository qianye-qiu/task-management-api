package cn.bugstack.exception;

//业务规则异常
public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String message){
        super(message);
    }
}
