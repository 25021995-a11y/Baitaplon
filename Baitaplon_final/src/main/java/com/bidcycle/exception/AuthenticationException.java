package com.bidcycle.exception;

/**
 * Ném ra khi có lỗi xác thực / phân quyền.
 * Ví dụ: tự đấu giá sản phẩm của mình, chưa đăng nhập, không có quyền.
 */
public class AuthenticationException extends BidCycleException {
    public static final String CODE_SELF_BID       = "AUTH_001";
    public static final String CODE_UNAUTHORIZED   = "AUTH_002";

    public AuthenticationException(String errorCode, String message) {
        super(errorCode, message);
    }

    /** Người dùng đấu giá sản phẩm của chính mình */
    public static AuthenticationException selfBid() {
        return new AuthenticationException(CODE_SELF_BID,
                "Bạn không thể đấu giá sản phẩm của chính mình!");
    }

    /** Chưa đăng nhập hoặc token hết hạn */
    public static AuthenticationException unauthorized() {
        return new AuthenticationException(CODE_UNAUTHORIZED,
                "Bạn cần đăng nhập để thực hiện hành động này!");
    }
}
