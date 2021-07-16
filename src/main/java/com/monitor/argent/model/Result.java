package com.monitor.argent.model;

import com.monitor.argent.api.Code;

import java.io.Serializable;

public final class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int code;
    private final T data;
    private String message;

    /**
     * 创建失败结果
     *
     * @param code 错误码
     * @return
     */
    public static <E> Result<E> failure(int code) {
        return new Result<>(code, null, null);
    }

    public static <E> Result<E> failure(int code, E data) {
        return new Result<>(code, null, data);
    }

    /**
     * 创建失败结果
     *
     * @param code 错误码
     * @param data 响应对象
     * @return
     */
    public static <E> Result<E> failure(int code, String message, E data) {
        return new Result<>(code, message, data);
    }

    /**
     * 创建失败结果
     *
     * @param code    错误码
     * @param message 错误信息
     * @return
     */
    public static <E> Result<E> failure(int code, String message) {
        return new Result<>(code, message, null);
    }

    @SuppressWarnings("unchecked")
    public static <E> Result<E> failure(Result src) {
        return (Result<E>) src;
    }

    /**
     * 创建成功结果
     *
     * @param data 要返回的结果
     * @return
     */
    public static <E> Result<E> success(E data) {
        return new Result<>(Code.SUCCESS, null, data);
    }

    public Result() {
        this(Code.SYSTEM_ERROR, null, null);
    }

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return code == Code.SUCCESS;
    }

    public int getCode() {
        return code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
