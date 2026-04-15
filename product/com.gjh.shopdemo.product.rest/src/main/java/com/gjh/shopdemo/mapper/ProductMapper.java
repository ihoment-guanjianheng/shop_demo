package com.gjh.shopdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjh.shopdemo.pojo.model.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}