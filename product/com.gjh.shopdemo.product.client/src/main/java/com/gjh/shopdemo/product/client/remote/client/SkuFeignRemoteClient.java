package com.gjh.shopdemo.product.client.remote.client;

import com.gjh.shopdemo.product.client.remote.SkuFeignRemote;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "product-service", path = "/sku")
public interface SkuFeignRemoteClient extends SkuFeignRemote {

}