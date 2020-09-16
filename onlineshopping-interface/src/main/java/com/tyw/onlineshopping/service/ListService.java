package com.tyw.onlineshopping.service;

import com.tyw.onlineshopping.bean.SkuLsInfo;
import com.tyw.onlineshopping.bean.SkuLsParams;
import com.tyw.onlineshopping.bean.SkuLsResult;

import java.io.IOException;

public interface ListService {

    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) throws IOException;

    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams) throws IOException;

    public void incrHotScore(String skuId) throws IOException;
}
