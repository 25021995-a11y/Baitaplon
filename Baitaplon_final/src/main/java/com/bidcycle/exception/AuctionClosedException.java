package com.bidcycle.exception;

/**
 * Ném ra khi phiên đấu giá đã đóng (kết thúc) hoặc sản phẩm đã kết thúc.
 */
public class AuctionClosedException extends BidCycleException {
    public static final String CODE_SESSION_ENDED     = "AUC_001";
    public static final String CODE_PRODUCT_FINISHED  = "AUC_002";

    public AuctionClosedException(String errorCode, String message) {
        super(errorCode, message);
    }

    /** Phiên đấu giá đã kết thúc */
    public static AuctionClosedException sessionEnded() {
        return new AuctionClosedException(CODE_SESSION_ENDED,
                "Phiên đấu giá đã kết thúc!");
    }

    /** Sản phẩm đã kết thúc phiên đấu giá */
    public static AuctionClosedException productAlreadyFinished(String productName) {
        return new AuctionClosedException(CODE_PRODUCT_FINISHED,
                "Sản phẩm \"" + productName + "\" đã kết thúc phiên đấu giá!");
    }
}