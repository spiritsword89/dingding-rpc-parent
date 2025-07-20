package com.dingding.exceptions;

public class ChannelNotRegisterException extends RuntimeException{
    public ChannelNotRegisterException(String message){
        super("The requested service: " + message + " is not registered");
    }
}
