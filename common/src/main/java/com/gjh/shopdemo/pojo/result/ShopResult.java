package com.gjh.shopdemo.pojo.result;

import com.gjh.shopdemo.pojo.enums.ResultEnum;
import lombok.Data;

/**
 * 统一返回结果
 * @param <T>
 */

@Data
public class ShopResult<T> {
    private Integer code;
    private String msg;
    private T data;

    public ShopResult(T data, ResultEnum resultEnum){
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getMessage();
        this.data = data;
    }

    public ShopResult(T data,String msg,Integer code){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


    public static <T> ShopResult<T> success(T data){
        return new ShopResult<>(data, ResultEnum.SUCCESS);
    }

    public static <T> ShopResult<T> fail(T data){
        return new ShopResult<>(data, ResultEnum.FAIL);
    }

    public static <T> ShopResult<T> fail(ResultEnum resultEnum){
        return new ShopResult<>(null, resultEnum);
    }

    public static <T> ShopResult<T> fail(Integer code, String msg){
        return new ShopResult<>(null, msg, code);
    }
}
