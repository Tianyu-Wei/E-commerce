package com.tyw.onlineshopping.cartweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tyw.onlineshopping.bean.CartInfo;
import com.tyw.onlineshopping.config.LoginRequire;
import com.tyw.onlineshopping.service.CartService;
import com.tyw.onlineshopping.util.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {

    @Reference
    CartService cartService;

    @PostMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addCart(@RequestParam("skuId") String skuId, @RequestParam("num") int num, HttpServletRequest request, HttpServletResponse response) throws InterruptedException {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
            //Check whether user has id, if not generate a new id, if yes use the exits id as keys of the cart
            if (userId == null) {
                userId = UUID.randomUUID().toString();
                CookieUtil.setCookie(request, response, "user_tmp_id", userId, 60*60*24*7, false);
            }
        }
        CartInfo cartInfo = cartService.addCart(userId, skuId, num);
        request.setAttribute("cartInfo", cartInfo);
        request.setAttribute("num", num);

        return "success";
    }

    @GetMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response) {
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = null;
        if (userId != null) { //If user logged in
            cartList = cartService.cartList(userId);
        }

        String userTmpId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
        List<CartInfo> cartTmpList = null;
        if (userTmpId != null) { // If temporary cart is not empty, get it merged with user's cart
            cartTmpList = cartService.cartList(userTmpId);
            cartList = cartTmpList;
        }
        if (userId != null && cartTmpList != null && cartTmpList.size() > 0) {
           cartList = cartService.mergeCartList(userId, userTmpId);
        }

            request.setAttribute("cartList", cartList);

        return "cartList";
    }

    @PostMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(@RequestParam("isChecked") String isChecked, @RequestParam("skuId") String skuId, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
        }

        cartService.checkCart(userId, skuId, isChecked);
    }

}
