package com.apigcc.example.common;

/**
 * @code
 */
public enum Code{

    OK(0,"ok"),
    ERROR(-1,"error"),
    NoAuth(1,"no auth");

    /**
     * 代码
     */
    private int code;
    /**
     * 文字信息
     */
    private String text;

    Code(int code, String text) {
        this.code = code;
        this.text = text;
    }
}