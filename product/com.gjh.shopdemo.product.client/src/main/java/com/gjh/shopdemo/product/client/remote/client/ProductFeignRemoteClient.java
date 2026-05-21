package com.gjh.shopdemo.product.client.remote.client;

import com.gjh.shopdemo.product.client.remote.ProductFeignRemote;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "product-service", contextId = "productFeignRemoteClient")
public interface ProductFeignRemoteClient extends ProductFeignRemote {
}