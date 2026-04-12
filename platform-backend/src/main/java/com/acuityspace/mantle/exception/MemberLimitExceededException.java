package com.acuityspace.mantle.exception;

public class MemberLimitExceededException extends RuntimeException {
    public MemberLimitExceededException(String message) {
        super(message);
    }
}
