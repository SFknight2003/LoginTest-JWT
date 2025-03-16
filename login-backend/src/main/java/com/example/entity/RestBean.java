package com.example.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

public record RestBean<T>(int code, T data, String message) {
    // 正确返回
    public static <T> RestBean <T> success(T data){
        return new RestBean<>(200,data,"请求成功");
    }
    public static <T> RestBean <T> success() {
        return success(null);
    }
    // 错误返回
    public static <T> RestBean <T> unauthorized(String message){
        return failure(401,message);
    };
    public static <T> RestBean <T> forbidden(String message){
        return failure(403,message);
    };
    public static <T> RestBean <T> failure(int code, String message){
        return new RestBean<>(code, null, message);    // 请求失败报错，没有data输出。message为标准
    }
    // 转变为json字符串
    public String asJsonString(){
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);   // 保留空值
    }
}
