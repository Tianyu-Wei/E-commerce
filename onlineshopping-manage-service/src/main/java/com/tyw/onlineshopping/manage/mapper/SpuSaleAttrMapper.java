package com.tyw.onlineshopping.manage.mapper;

import com.tyw.onlineshopping.bean.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {

    public List<SpuSaleAttr> getSpuSaleAttrListBySpuId(String spuId);
    public List<SpuSaleAttr> getSpuSaleAttrListBySpuIdCheckSku(@Param("skuId") String skuId, @Param("spuId") String spuId);
}
