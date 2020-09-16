package com.tyw.onlineshopping.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.tyw.onlineshopping.bean.BaseAttrInfo;
import com.tyw.onlineshopping.bean.BaseAttrValue;
import com.tyw.onlineshopping.bean.SkuLsParams;
import com.tyw.onlineshopping.bean.SkuLsResult;
import com.tyw.onlineshopping.service.ListService;
import com.tyw.onlineshopping.service.ManageService;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    ListService listService;

    @Reference
    ManageService manageService;

    @GetMapping("list.html")
    public String list(SkuLsParams skuLsParams, Model model) throws IOException {
        skuLsParams.setPageSize(2);
        SkuLsResult skuLsResult = listService.getSkuLsInfoList(skuLsParams);
        model.addAttribute("skuLsResult", skuLsResult);
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);
        model.addAttribute("attrList", attrList);

        String paramUrl = makeParamUrl(skuLsParams);

        //selected attributes of platform
        List<BaseAttrValue> selectedValueList = new ArrayList();


        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                        String selectedValueId = skuLsParams.getValueId()[i];
                        if (baseAttrValue.getId().equals(selectedValueId)) {
                            iterator.remove(); //delete attributes
                            String selectedParamUrl = makeParamUrl(skuLsParams, selectedValueId);
                            baseAttrValue.setParamUrl(selectedParamUrl);
                            selectedValueList.add(baseAttrValue);//add selected lists
                        }
                    }
                }
            }
        }

        model.addAttribute("paramUrl", paramUrl);

        model.addAttribute("selectedValueIdList", selectedValueList);

        model.addAttribute("keyword", skuLsParams.getKeyword());

        model.addAttribute("pageNo", skuLsParams.getPageNo());

        model.addAttribute("totalPages", skuLsResult.getTotalPages());

        return "list";
    }

    public String makeParamUrl(SkuLsParams skuLsParams, String... excludeValueId) {
        String paramUrl = "";
        if (skuLsParams.getKeyword() != null) {
            paramUrl += "keyword=" + skuLsParams.getKeyword();
        } else if (skuLsParams.getCatalog3Id()!= null) {
            paramUrl += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }

        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                if (excludeValueId!=null && excludeValueId.length > 0) {
                    String exValueId = excludeValueId[0];
                    if (valueId.equals(exValueId)) {
                        continue;
                    }
                }

                if (paramUrl.length() > 0) {
                    paramUrl += "&";
                }

                paramUrl += "valueId="+valueId;
            }
        }
        return paramUrl;
    }
}
