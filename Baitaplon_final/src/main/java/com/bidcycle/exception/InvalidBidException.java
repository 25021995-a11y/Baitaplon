package com.bidcycle.exception;

/**
 * Ném ra khi hành động đặt giá vi phạm quy tắc về giá.
 * Ví dụ: giá quá thấp, số tiền không hợp lệ, số dư không đủ.
 */
public class InvalidBidException extends BidCycleException {
    public static final String CODE_PRICE_TOO_LOW   = "BID_002";
    public static final String CODE_INSUFFICIENT    = "BID_004";
    public static final String CODE_INVALID_AMOUNT  = "BID_005";

    private final double currentPrice;
    private final double attemptedPrice;

    public InvalidBidException(String errorCode, String message) {
        super(errorCode, message);
        this.currentPrice = 0;
        this.attemptedPrice = 0;
    }

    public InvalidBidException(String errorCode, String message,
                               double currentPrice, double attemptedPrice) {
        super(errorCode, message);
        this.currentPrice = currentPrice;
        this.attemptedPrice = attemptedPrice;
    }

    /** Giá đặt không cao hơn giá hiện tại */
    public static InvalidBidException priceTooLow(double currentPrice, double attemptedPrice) {
        return new InvalidBidException(CODE_PRICE_TOO_LOW,
                String.format("Giá đặt ($%.2f) phải cao hơn giá hiện tại ($%.2f)!",
                        attemptedPrice, currentPrice),
                currentPrice, attemptedPrice);
    }

    /** Số dư không đủ */
    public static InvalidBidException insufficientFunds(double available, double required) {
        return new InvalidBidException(CODE_INSUFFICIENT,
                String.format("Số dư khả dụng không đủ! Cần: $%.2f, Hiện có: $%.2f",
                        required, available));
    }

    /** Giá trị đặt không hợp lệ (âm hoặc bằng 0) */
    public static InvalidBidException invalidAmount(double amount) {
        return new InvalidBidException(CODE_INVALID_AMOUNT,
                String.format("Giá đặt không hợp lệ: $%.2f. Giá phải là số dương!", amount));
    }

    public double getCurrentPrice()   { return currentPrice; }
    public double getAttemptedPrice() { return attemptedPrice; }
}