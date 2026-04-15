package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.mapper.SkuMapper;
import com.gjh.shopdemo.pojo.model.Sku;
import com.gjh.shopdemo.service.SkuService;
import org.springframework.stereotype.Service;

@Service
public class SkuServiceImpl extends ServiceImpl<SkuMapper, Sku> implements SkuService {
}