package com.gjh.shopdemo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gjh.shopdemo.pojo.dto.SkuAddDTO;
import com.gjh.shopdemo.pojo.dto.SkuPageQueryDTO;
import com.gjh.shopdemo.pojo.dto.SkuUpdateDTO;
import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.model.Sku;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.product.client.remote.SkuFeignRemote;
import com.gjh.shopdemo.product.client.remote.pojo.dto.SkuStockDTO;
import com.gjh.shopdemo.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * SKU管理接口
 */
@RestController
@RequestMapping("/sku")
public class SkuController implements SkuFeignRemote {

    @Autowired
    private SkuService skuService;

    /**
     * 新增SKU
     */
    @PostMapping("/add")
    public ShopResult<Void> add(@Valid @RequestBody SkuAddDTO dto) {
        skuService.addSku(dto);
        return ShopResult.success();
    }

    /**
     * 分页查询SKU列表
     */
    @GetMapping("/page")
    public ShopResult<IPage<Sku>> page(SkuPageQueryDTO dto) {
        return ShopResult.success(skuService.pageQuery(dto));
    }

    /**
     * 查询SKU详情
     */
    @GetMapping("/{id}")
    public ShopResult<Sku> getById(@PathVariable Long id) {
        Sku sku = skuService.getById(id);
        if (sku == null) {
            throw new BaseException("SKU不存在");
        }
        return ShopResult.success(sku);
    }

    /**
     * 调整SKU库存（增量）
     */
    @PutMapping("/{id}/stock")
    public ShopResult<Void> deltaStock(@PathVariable Long id, @RequestParam Integer delta) {
        skuService.deltaStock(id, delta);
        return ShopResult.success();
    }

    /**
     * 修改SKU信息
     */
    @PutMapping("/{id}")
    public ShopResult<Void> update(@PathVariable Long id, @Valid @RequestBody SkuUpdateDTO dto) {
        dto.setId(id);
        skuService.updateSku(dto);
        return ShopResult.success();
    }

    @Override
    @GetMapping("/getStockById/{id}")
    public ShopResult<SkuStockDTO> getStockById(Long id) {
        return ShopResult.success(skuService.getStockById(id));
    }

    @Override
    @PostMapping("/deductDbStock/{id}")
    public ShopResult<Void> deductDbStock(@PathVariable Long id, @RequestParam Integer quantity) {
        skuService.deductDbStock(id, quantity);
        return ShopResult.success();
    }

    @Override
    @PostMapping("/addDbStock/{id}")
    public ShopResult<Void> addDbStock(@PathVariable Long id, @RequestParam Integer quantity) {
        skuService.addDbStock(id, quantity);
        return ShopResult.success();
    }
}