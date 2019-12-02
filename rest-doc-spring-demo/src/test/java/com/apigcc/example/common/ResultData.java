package com.apigcc.example.common;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResultData<T> {

    /**
     * 返回码
     */
    int code;
    //返回信息
    String msg;
    T data;

    public static <T> ResultData<T> ok(){
        return ok(null);
    }
    public static <T> ResultData<T> ok(T data){
        ResultData<T> resultData = new ResultData<>();
        resultData.code = 0;
        resultData.msg = "ok";
        resultData.data = data;
        return resultData;
    }

}
