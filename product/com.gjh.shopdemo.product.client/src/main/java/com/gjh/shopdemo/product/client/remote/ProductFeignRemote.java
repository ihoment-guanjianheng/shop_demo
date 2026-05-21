package com.gjh.shopdemo.product.client.remote;

import com.gjh.shopdemo.pojo.result.ShopResult;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public interface ProductFeignRemote {

    @GetMapping("/myIds")
    ShopResult<List<Long>> getMyProductIds();
}