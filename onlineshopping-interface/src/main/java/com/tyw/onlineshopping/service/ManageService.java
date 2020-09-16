package com.tyw.onlineshopping.service;

import com.tyw.onlineshopping.bean.*;

import java.util.List;
import java.util.Map;

public interface ManageService {
    //Search level 1 category
    public List<BaseCatalog1> getCatalog1();

    //Search level 2 category
    public List<BaseCatalog2> getCatalog2(String catalogId);

    //Search level 3 category
    public List<BaseCatalog3> getCatalog3(String catalogId);

    //Get Attributes from the category we choose
    public List<BaseAttrInfo> getAttrList(String catalogId);

    //Save attributes transfered from Frontend
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    //Search attributealist from selected category
    public BaseAttrInfo getBaseAttrInfo(String attrId);

    //To get basic selling attribute
    public List<BaseSaleAttr> getBaseSaleAttrList();

    //Save Spu attributes
    public void saveSpuInfo(SpuInfo spuInfo);

    //check SpuList
    public List<SpuInfo> getSpuList(String catalog3Id);

    //Return Spu image List
    public List<SpuImage> getSpuImageList(String spuId);

    //Search saleAttributes based on spuId
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    //Save sku information
    public void saveSkuInfo(SkuInfo skuInfo);

    //Search sku
    public SkuInfo getSkuInfo(String skuId) throws InterruptedException;

    //Check selling attributes based on spuId, pass selling attributes when it was checked
    public List<SpuSaleAttr> getSpuSaleAttrListCheckSku(String skuId, String spuId);

    public Map getSkuValueIdsMap(String spuId);

    public List<BaseAttrInfo> getAttrList(List attrValueIdList);

}
