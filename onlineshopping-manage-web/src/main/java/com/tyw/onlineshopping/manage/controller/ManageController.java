package com.tyw.onlineshopping.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tyw.onlineshopping.bean.*;
import com.tyw.onlineshopping.service.ListService;
import com.tyw.onlineshopping.service.ManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@CrossOrigin
public class ManageController {

    @Reference
    ManageService manageService;

    @Reference
    ListService listService;

    @PostMapping("getCatalog1")
    public List<BaseCatalog1> getBaseCatalog1() {
        List<BaseCatalog1> baseCatalog1List = manageService.getCatalog1();
        return baseCatalog1List;
    }

    @PostMapping("getCatalog2")
    public List<BaseCatalog2> getBaseCatalog2(String catalog1Id) {
        List<BaseCatalog2> baseCatalog2List = manageService.getCatalog2(catalog1Id);
        return baseCatalog2List;
    }

    @PostMapping("getCatalog3")
    public List<BaseCatalog3> getBaseCatalog3(String catalog2Id) {
        List<BaseCatalog3> baseCatalog3List = manageService.getCatalog3(catalog2Id);
        return baseCatalog3List;
    }

    @GetMapping("attrInfoList")
    public List<BaseAttrInfo> getBaseAttrInfoList(String catalog3Id) {
        List<BaseAttrInfo> attrInfoList = manageService.getAttrList(catalog3Id);
        return attrInfoList;
    }

    @PostMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        manageService.saveAttrInfo(baseAttrInfo);
        return "success";
    }

    @PostMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(@RequestParam("attrId") String attrId) {
        BaseAttrInfo baseAttrInfo = manageService.getBaseAttrInfo(attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();
        return baseAttrValueList;
    }

    @PostMapping("baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return manageService.getBaseSaleAttrList();

    }

    @PostMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        manageService.saveSpuInfo(spuInfo);
        return "success";
    }

    @GetMapping("spuList")
    public List<SpuInfo> getSpuList(String catalog3Id) {
        return manageService.getSpuList(catalog3Id);

    }

    @GetMapping("spuImageList")
    public List<SpuImage> getSpuImageList(String spuId) {
        return manageService.getSpuImageList(spuId);

    }

    @GetMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return manageService.getSpuSaleAttrList(spuId);
    }

    public String onSaleBySpu(String spuId){
        return null;
    }

    @PostMapping("onSale")
    public String onSale(@RequestParam("skuId") String skuId) throws IOException, InterruptedException {
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        SkuLsInfo skuLsInfo = new SkuLsInfo();
        try {
            BeanUtils.copyProperties(skuLsInfo, skuInfo);
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuLsInfo(skuLsInfo);
        return "success";
    }

}
