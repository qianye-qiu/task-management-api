package cn.bugstack.exception;

//资源不存在异常
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resourceName, Object id){
        super(resourceName + "not found, id:" + id);
    }
}
