package com.dingding.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RpcMethodDescriptor {
    private String methodId;
    private String className;
    private Integer numOfParams;
    private String methodName;
    private List<String> parameterTypes;
    public boolean hasReturnValue;
    private String returnValueType;

    public static String generateMethodId(String methodName, int paramsCount, String[] paramTypeSimpleNames, String returnValueTypeSimpleName) {
        String id = String.join(".", methodName, String.valueOf(paramsCount));
        for(String paramTypeSimpleName:  paramTypeSimpleNames){
            id = String.join(".", id, paramTypeSimpleName);
        }

        if(returnValueTypeSimpleName != null) {
            id = String.join(".", id, returnValueTypeSimpleName);
        }

        return id;
    }

    public static RpcMethodDescriptor build(Method method){
        RpcMethodDescriptor md = new RpcMethodDescriptor();

        Class<?>[] paramTypes = method.getParameterTypes();

        md.setClassName(method.getDeclaringClass().getName());
        md.setNumOfParams(method.getParameterCount());
        md.setMethodName(method.getName());

        //id = method name + "." + method parameters count + "." + (参数的simple name，并且以.隔开) + "." + return value type
        String id = String.join(".", method.getName(), String.valueOf(method.getParameterCount()));

        List<String> parameterTypes = new ArrayList<>();
        for(Class<?> param:  paramTypes) {
            id = String.join(".", id, param.getSimpleName());
            parameterTypes.add(param.getSimpleName());
        }

        md.setHasReturnValue(false);

        if(!method.getReturnType().equals(Void.class)) {
            md.setHasReturnValue(true);
            md.setReturnValueType(method.getReturnType().getName());
            id = String.join(".", id, method.getReturnType().getSimpleName());
        }

        md.setMethodId(id);

        md.setParameterTypes(parameterTypes);

        return md;
    }

    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getNumOfParams() {
        return numOfParams;
    }

    public void setNumOfParams(Integer numOfParams) {
        this.numOfParams = numOfParams;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public boolean isHasReturnValue() {
        return hasReturnValue;
    }

    public void setHasReturnValue(boolean hasReturnValue) {
        this.hasReturnValue = hasReturnValue;
    }

    public String getReturnValueType() {
        return returnValueType;
    }

    public void setReturnValueType(String returnValueType) {
        this.returnValueType = returnValueType;
    }
}
