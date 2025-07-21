package com.dingding.exceptions;

public class RequestClassNotDetermineException extends RuntimeException {
    public RequestClassNotDetermineException() {
        super("The Requested method is found in two or more implementations");
    }
}
