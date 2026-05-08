package com.gjh.shopdemo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gjh.shopdemo.pojo.dto.ProductAddDTO;
import com.gjh.shopdemo.pojo.dto.ProductPageQueryDTO;
import com.gjh.shopdemo.pojo.dto.ProductUpdateDTO;
import com.gjh.shopdemo.pojo.model.Product;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.pojo.vo.ProductDetailVO;
import com.gjh.shopdemo.service.ProductService;
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
 * 商品管理接口
 */
@RestController
@RequestMapping
public class ProductController{

    @Autowired
    private ProductService productService;

    /**
     * 新增商品
     */
    @PostMapping("/add")
    public ShopResult<Void> add(@Valid @RequestBody ProductAddDTO dto) {
        productService.addProduct(dto);
        return ShopResult.success();
    }

    /**
     * 分页查询商品列表
     */
    @GetMapping("/page")
    public ShopResult<IPage<Product>> page(ProductPageQueryDTO dto) {
        return ShopResult.success(productService.pageQuery(dto));
    }

    /**
     * 查询商品详情
     */
    @GetMapping("/{id}")
    public ShopResult<ProductDetailVO> detail(@PathVariable Long id) {
        return ShopResult.success(productService.detail(id));
    }

    /**
     * 修改商品状态
     */
    @PutMapping("/{id}/status")
    public ShopResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return ShopResult.success();
    }

    /**
     * 修改商品信息
     */
    @PutMapping("/{id}")
    public ShopResult<Void> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateDTO dto) {
        dto.setId(id);
        productService.updateProduct(dto);
        return ShopResult.success();
    }
}