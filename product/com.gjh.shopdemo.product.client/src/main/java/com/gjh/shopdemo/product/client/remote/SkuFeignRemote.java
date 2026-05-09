package com.gjh.shopdemo.product.client.remote;

import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.product.client.remote.pojo.vo.SkuStockVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface SkuFeignRemote {

    @GetMapping("/getStockById/{id}")
    ShopResult<SkuStockVO> getStockById(@PathVariable("id") Long id);

    /**
     * 仅扣减数据库库存（不操作 Redis 缓存）
     */
    @PostMapping("/deductDbStock/{id}")
    ShopResult<Void> deductDbStock(@PathVariable("id") Long id, @RequestParam Integer quantity);

    /**
     * 仅增加数据库库存（不操作 Redis 缓存）
     */
    @PostMapping("/addDbStock/{id}")
    ShopResult<Void> addDbStock(@PathVariable("id") Long id, @RequestParam Integer quantity);
}
