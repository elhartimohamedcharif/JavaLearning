package org.liuyehcf.jdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by Liuye on 2017/12/24.
 */
public class JdkProxyHandler implements InvocationHandler {
    public JdkProxyHandler(Object obj) {
        this.obj = obj;
    }

    private Object obj;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("pre-processor");

        Object result = method.invoke(obj, args);

        System.out.println("after-processor");

        return result;
    }
}