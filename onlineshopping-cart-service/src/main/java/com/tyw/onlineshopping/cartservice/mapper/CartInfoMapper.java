package com.tyw.onlineshopping.cartservice.mapper;

import com.tyw.onlineshopping.bean.CartInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


public interface CartInfoMapper extends Mapper<CartInfo> {

    public List<CartInfo> selectCartListWithSkuPrice(String userId);

    public void mergeCartList(@Param("userIdDest") String  userIdDest,@Param("userIdOrig") String userIdOrig);
}
