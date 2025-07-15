package com.dingding.model;

import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;

public class MessagePayload implements Serializable {
    private String clientId; // consumer id
    @JSONField(serializeUsing = MessageTypeSerializer.class, deserializeUsing = MessageTypeDeserializer.class)
    private MessageType messageType;
    private Object payload;

    public MessagePayload() {

    }

    public MessagePayload(MessageBuilder builder) {
        this.clientId = builder.clientId;
        this.messageType = builder.messageType;
        if(messageType.equals(MessageType.CALL)) {
            this.payload = new RpcRequest(builder);
        } else if (messageType.equals(MessageType.RESPONSE)) {
            this.payload = new RpcResponse(builder);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public static class MessageBuilder {
        private MessageType messageType;
        private String clientId;
        private String requestClientId;
        private String requestId;
        private String requestMethodSimpleName;
        private String requestClassName;
        private String returnValueType;
        private String[] paramTypes;
        private Object[] params;
        private Object returnValue;

        public MessageBuilder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public MessageBuilder setRequestClientId(String requestClientId) {
            this.requestClientId = requestClientId;
            return this;
        }

        public MessageBuilder setReturnValueType(String returnValueType) {
            this.returnValueType = returnValueType;
            return this;
        }

        public MessageBuilder setMessageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public MessageBuilder setRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public MessageBuilder setRequestMethodName(String requestMethodName) {
            this.requestMethodSimpleName = requestMethodName;
            return this;
        }

        public MessageBuilder setRequestedClassName(String requestedClassName) {
            this.requestClassName = requestedClassName;
            return this;
        }

        public MessageBuilder setParamTypes(String[] paramTypes) {
            this.paramTypes = paramTypes;
            return this;
        }

        public MessageBuilder setParams(Object[] params) {
            this.params = params;
            return this;
        }

        public MessageBuilder setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
            return this;
        }

        public MessagePayload build() {
            return new MessagePayload(this);
        }
    }

    public static class RpcRequest implements Serializable {
        private String requestClientId; // producer id
        private String requestId;
        private String requestMethodSimpleName;
        private String requestClassName;
        private String returnValueType;
        private String[] paramTypes;
        private Object[] params;

        public RpcRequest() {
        }

        public RpcRequest(MessageBuilder builder) {
            this.requestClientId = builder.requestClientId;
            this.requestId = builder.requestId;
            this.requestMethodSimpleName = builder.requestMethodSimpleName;
            this.requestClassName = builder.requestClassName;
            this.paramTypes = builder.paramTypes;
            this.params = builder.params;
            this.returnValueType = builder.returnValueType;
        }

        public String getRequestClientId() {
            return requestClientId;
        }

        public void setRequestClientId(String requestClientId) {
            this.requestClientId = requestClientId;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getRequestMethodSimpleName() {
            return requestMethodSimpleName;
        }

        public void setRequestMethodSimpleName(String requestMethodSimpleName) {
            this.requestMethodSimpleName = requestMethodSimpleName;
        }

        public String getRequestClassName() {
            return requestClassName;
        }

        public void setRequestClassName(String requestClassName) {
            this.requestClassName = requestClassName;
        }

        public String getReturnValueType() {
            return returnValueType;
        }

        public void setReturnValueType(String returnValueType) {
            this.returnValueType = returnValueType;
        }

        public String[] getParamTypes() {
            return paramTypes;
        }

        public void setParamTypes(String[] paramTypes) {
            this.paramTypes = paramTypes;
        }

        public Object[] getParams() {
            return params;
        }

        public void setParams(Object[] params) {
            this.params = params;
        }
    }

    public static class RpcResponse implements Serializable {
        private String requestId;
        private Object returnValue;

        public RpcResponse() {

        }

        public RpcResponse(MessageBuilder builder) {
            this.requestId = builder.requestId;
            this.returnValue = builder.returnValue;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public Object getReturnValue() {
            return returnValue;
        }

        public void setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
        }
    }
}
