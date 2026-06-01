package com.bidcycle.exception;

/**
 * Exception gốc của hệ thống BidCycle.
 * Tất cả custom exceptions đều kế thừa từ lớp này.
 */
public class BidCycleException extends RuntimeException {
    private final String errorCode;

    public BidCycleException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BidCycleException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "[" + errorCode + "] " + getMessage();
    }
}