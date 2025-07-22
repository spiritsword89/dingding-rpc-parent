package com.dingding.config;

import java.lang.reflect.Field;
import java.util.Objects;

public class RemoteServiceFieldHolder {
    private Field remoteServiceField;
    private String requestClientId;
    private String fieldClassName;
    private String alias;

    public RemoteServiceFieldHolder(Field remoteServiceField, String requestClientId) {
        this.remoteServiceField = remoteServiceField;
        this.requestClientId = requestClientId;
        this.fieldClassName = remoteServiceField.getType().getName();
        this.alias = remoteServiceField.getName();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fieldClassName);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(!(obj instanceof RemoteServiceFieldHolder holder)) {
            return false;
        }

        return this.fieldClassName.equals(holder.fieldClassName);
    }

    public Field getRemoteServiceField() {
        return remoteServiceField;
    }

    public void setRemoteServiceField(Field remoteServiceField) {
        this.remoteServiceField = remoteServiceField;
    }

    public String getRequestClientId() {
        return requestClientId;
    }

    public void setRequestClientId(String requestClientId) {
        this.requestClientId = requestClientId;
    }

    public String getFieldClassName() {
        return fieldClassName;
    }

    public void setFieldClassName(String fieldClassName) {
        this.fieldClassName = fieldClassName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
