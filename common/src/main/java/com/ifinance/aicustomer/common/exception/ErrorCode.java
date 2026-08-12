package com.ifinance.aicustomer.common.exception;

/**
 * 业务错误码。
 */
public enum ErrorCode {

    SUCCESS(0, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统繁忙，请稍后重试"),
    AI_SERVICE_ERROR(10001, "AI 服务调用失败"),
    DATA_ACCESS_ERROR(10002, "数据访问失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
