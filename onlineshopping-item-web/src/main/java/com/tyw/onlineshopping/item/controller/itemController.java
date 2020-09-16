package com.tyw.onlineshopping.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.tyw.onlineshopping.bean.SkuInfo;
import com.tyw.onlineshopping.bean.SpuSaleAttr;
import com.tyw.onlineshopping.config.LoginRequire;
import com.tyw.onlineshopping.service.ListService;
import com.tyw.onlineshopping.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class itemController {

    @Reference
    ManageService manageService;

//    @Reference
//    ListService listService;

    @GetMapping("{skuId}.html")
    public String item(@PathVariable("skuId") String skuId, HttpServletRequest httpServletRequest) throws InterruptedException, IOException {
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckSku(skuId, skuInfo.getSpuId());
        httpServletRequest.setAttribute("spuSaleAttrList", spuSaleAttrList);
        httpServletRequest.setAttribute("skuInfo", skuInfo);
        Map skuValueIdsMap = manageService.getSkuValueIdsMap(skuInfo.getSpuId());
        String skuValueIdsJson = JSON.toJSONString(skuValueIdsMap);
        httpServletRequest.setAttribute("valuesSkuJson", skuValueIdsJson);

//        listService.incrHotScore(skuId);
        httpServletRequest.getAttribute("userId");
       return "item";
    }
}
