package org.apache.ibatis.exceptions;


import org.apache.ibatis.executor.ErrorContext;

// 异常工厂 —— 包装异常成PersistenceException
public class ExceptionFactory {

    private ExceptionFactory() {

    }

    public static RuntimeException wrapException(String message, Exception e) {
        return new PersistenceException(ErrorContext.instance().message(message).cause(e).toString(),e);
    }
}
