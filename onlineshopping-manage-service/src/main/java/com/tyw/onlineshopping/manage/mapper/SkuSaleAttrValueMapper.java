package com.tyw.onlineshopping.manage.mapper;

import com.tyw.onlineshopping.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

    public List<Map> getSaleAttrValuesBySpu(String spuId);

}
