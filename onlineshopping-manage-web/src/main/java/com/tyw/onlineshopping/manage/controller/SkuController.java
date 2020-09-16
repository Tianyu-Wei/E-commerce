package com.tyw.onlineshopping.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tyw.onlineshopping.bean.SkuInfo;
import com.tyw.onlineshopping.service.ManageService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SkuController {

    @Reference
    ManageService manageService;

    @PostMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        manageService.saveSkuInfo(skuInfo);
        return "success";
    }
}
