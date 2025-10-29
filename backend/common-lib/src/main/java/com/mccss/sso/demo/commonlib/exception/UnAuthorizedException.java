package com.mccss.sso.demo.commonlib.exception;

public class UnAuthorizedException extends ApplicationException {

    public UnAuthorizedException(String message) {
        super(message);
    }

    public UnAuthorizedException(int statusCode, String message) {
        super(statusCode, message);
    }
}
