package com.gjh.shopdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjh.shopdemo.pojo.model.Sku;
import com.gjh.shopdemo.pojo.dto.SkuAddDTO;

public interface SkuService extends IService<Sku> {

    void addSku(SkuAddDTO dto);
}