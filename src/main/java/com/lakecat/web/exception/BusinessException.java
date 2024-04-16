package com.lakecat.web.exception;


import org.apache.thrift.TException;

public class BusinessException extends TException {

    private int value;

    public BusinessException() {
        super();
    }

    public BusinessException(String msg, int value) {
        super(msg);
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}