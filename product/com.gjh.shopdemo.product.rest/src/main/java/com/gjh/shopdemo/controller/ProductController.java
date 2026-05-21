package com.gjh.shopdemo.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gjh.shopdemo.context.AuthContext;
import com.gjh.shopdemo.oss.OssService;
import com.gjh.shopdemo.oss.PresignVO;
import com.gjh.shopdemo.pojo.dto.ProductAddDTO;
import com.gjh.shopdemo.pojo.dto.ProductPageQueryDTO;
import com.gjh.shopdemo.pojo.dto.ProductUpdateDTO;
import com.gjh.shopdemo.pojo.model.Product;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.pojo.vo.ProductDetailVO;
import com.gjh.shopdemo.pojo.vo.UserInfoVO;
import com.gjh.shopdemo.product.client.remote.ProductFeignRemote;
import com.gjh.shopdemo.product.client.remote.client.ProductFeignRemoteClient;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品管理接口
 */
@RestController
@RequestMapping
public class ProductController implements ProductFeignRemote {

    private static final long PRESIGN_EXPIRES_SECONDS = 300L;

    @Autowired
    private ProductService productService;

    @Autowired
    private OssService ossService;

    /**
     * 新增商品
     */
    @PostMapping("/add")
    public ShopResult<Void> add(@Valid @RequestBody ProductAddDTO dto) {
        productService.addProduct(dto);
        return ShopResult.success();
    }

    /**
     * 查询当前用户的所有商品ID列表
     */
    @GetMapping("/myIds")
    public ShopResult<List<Long>> getMyProductIds() {
        UserInfoVO currentUser = AuthContext.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            return ShopResult.success(java.util.Collections.emptyList());
        }
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getUserId, currentUser.getId());
        List<Long> ids = productService.list(wrapper).stream().map(Product::getId).collect(Collectors.toList());
        return ShopResult.success(ids);
    }

    /**
     * 分页查询当前用户的商品列表
     */
    @GetMapping("/myPage")
    public ShopResult<IPage<Product>> myPage(ProductPageQueryDTO dto) {
        UserInfoVO currentUser = AuthContext.getCurrentUser();
        if (currentUser != null && currentUser.getId() != null) {
            dto.setUserId(currentUser.getId());
        }
        return ShopResult.success(productService.pageQuery(dto));
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
    @SentinelResource(value = "productDetail", blockHandler = "detailBlock")
    public ShopResult<ProductDetailVO> detail(@PathVariable Long id) {
        return ShopResult.success(productService.detail(id));
    }

    /**
     * 商品详情限流/熔断兜底方法
     */
    public ShopResult<ProductDetailVO> detailBlock(Long id, BlockException e) {
        return ShopResult.fail(429, "访问过于频繁,请稍后再试");
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

    /**
     * 获取商品主图预签名 PUT URL
     */
    @GetMapping("/image/presign")
    public ShopResult<PresignVO> imagePresign(@RequestParam String filename) {
        return ShopResult.success(ossService.presign("products/images", filename, PRESIGN_EXPIRES_SECONDS));
    }

    /**
     * 导出商品列表 Excel，文件上传 OSS 后返回公网下载链接
     */
    @GetMapping("/export")
    public ShopResult<String> export() {
        return ShopResult.success(productService.export());
    }
}