package org.example.servinet.core.entities;

import org.example.servinet.core.enums.ExceptionType;

import static org.example.servinet.utils.IdGenerate.getNewId;

public class Exception {
    private String customMessage;
    private String customTitle;
    private String id;
    private ExceptionType exception;

    Exception(String customMessage, String customTitle, ExceptionType exception){
        this.customMessage = customMessage;
        this.customTitle = customTitle;
        this.exception = exception;
        this.id =  getNewId();
    }
    Exception(ExceptionType exception){
        this.exception = exception;
        this.id =  getNewId();
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public String getCustomTitle() {
        return customTitle;
    }

    public String getId() {
        return id;
    }

    public ExceptionType getException() {
        return exception;
    }
}
