package com.springproject.profEcomWebApp.exception;

public class APIExceptionHandler extends  RuntimeException{
    private static final Long serialVersionUID = 1L;

    public APIExceptionHandler(){
    }

    public APIExceptionHandler(String message) {
        super(message);
    }
}
