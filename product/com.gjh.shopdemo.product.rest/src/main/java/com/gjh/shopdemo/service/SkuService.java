package com.gjh.shopdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjh.shopdemo.pojo.dto.SkuAddDTO;
import com.gjh.shopdemo.pojo.dto.SkuPageQueryDTO;
import com.gjh.shopdemo.pojo.dto.SkuUpdateDTO;
import com.gjh.shopdemo.pojo.model.Sku;
import com.gjh.shopdemo.product.client.remote.pojo.vo.SkuStockVO;

public interface SkuService extends IService<Sku> {

    void addSku(SkuAddDTO dto);

    IPage<Sku> pageQuery(SkuPageQueryDTO dto);

    void deltaStock(Long id, Integer delta);

    void updateSku(SkuUpdateDTO dto);

    SkuStockVO getStockById(Long id);

    void deductDbStock(Long id, Integer quantity);

    void addDbStock(Long id, Integer quantity);

    void deleteSku(Long id);
}