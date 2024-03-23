package com.gameverse.gameservice.exception;

public class RetryWorkFlowException extends RuntimeException{
    public RetryWorkFlowException(String message) {
        super(message);
    }
}
