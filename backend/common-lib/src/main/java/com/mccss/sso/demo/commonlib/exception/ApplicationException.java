package com.mccss.sso.demo.commonlib.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationException extends RuntimeException{

    private int statusCode;

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
