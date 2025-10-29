package com.mccss.sso.demo.commonlib.exception;

public class ForbiddenException extends ApplicationException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(int statusCode, String message) {
        super(statusCode, message);
    }
}
