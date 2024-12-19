package com.smartroute.book_network.handler;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;

import lombok.Getter;

public enum BusinessErrorCodes {

    NO_CODE(0, NOT_IMPLEMENTED, "No code "),
    INCORRECT_CURRENT_PASSWORD(300, BAD_REQUEST, "Incorrect current password"),
    NEW_PASSWORD_DOES_NOT_MATCH(301, BAD_REQUEST, "New password does not match"),
    ACCOUNT_LOCKED(302, FORBIDDEN, "Account is locked"),
    ACCOUNT_DISABLED(303, FORBIDDEN, "Account is disabled"),
    BAD_CREDENTIALS(304, FORBIDDEN, "Login failed. Bad credentials"),
    ;

    @Getter
    private final int code;
    @Getter
    private final String description;
    @Getter
    private final HttpStatus httpStatus;

    BusinessErrorCodes(int code, HttpStatus httpStatus, String description) {
        this.code = code;
        this.description = description;
        this.httpStatus = httpStatus;
    }

}
