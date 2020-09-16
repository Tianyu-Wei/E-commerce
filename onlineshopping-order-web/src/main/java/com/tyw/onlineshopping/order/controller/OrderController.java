package com.tyw.onlineshopping.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tyw.onlineshopping.bean.*;
import com.tyw.onlineshopping.bean.enums.OrderStatus;
import com.tyw.onlineshopping.bean.enums.ProcessStatus;
import com.tyw.onlineshopping.config.LoginRequire;
import com.tyw.onlineshopping.service.CartService;
import com.tyw.onlineshopping.service.ManageService;
import com.tyw.onlineshopping.service.OrderService;
import com.tyw.onlineshopping.service.UserService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    ManageService manageService;

    @GetMapping("trade")
    @LoginRequire
    public String trader(HttpServletRequest request) {
        String userid = (String) request.getAttribute("userId");
        //User address list
        //The list that need to be check out by users
        List<UserAddress> userAddressList = userService.getUserAddressList(userid);

        request.setAttribute("userAddressList", userAddressList);
        List<CartInfo> checkedCartList = cartService.getCheckedCartList(userid);
        BigDecimal totalAmount = new BigDecimal("0");
        for (CartInfo cartInfo : checkedCartList) {
            BigDecimal cartInfoAmount = cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
            totalAmount = totalAmount.add(cartInfoAmount);
        }

        String token = orderService.genToken(userid);

        request.setAttribute("tradeNo", token);
        request.setAttribute("checkedCartList", checkedCartList);
        request.setAttribute("totalAmount", totalAmount);

        return "trade";
    }

    @PostMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request) throws InterruptedException {

        String userId = (String) request.getAttribute("userId");
        String tradeNo = request.getParameter("tradeNo");
        boolean isAvailableToken = orderService.verifyToken(userId, tradeNo);
        if (!isAvailableToken) {
            request.setAttribute("errMsg", "Session has expired, please do check out again!");
            return "tradeFail";
        }

        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setCreateTime(new Date());
        orderInfo.setExpireTime(DateUtils.addMinutes(new Date(), 15));
        orderInfo.sumTotalAmount();

        orderInfo.setUserId(userId);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());
            orderDetail.setSkuName(skuInfo.getSkuName());

            if (!orderDetail.getSkuPrice().equals(skuInfo.getPrice())) {
                request.setAttribute("errMsg", "The price of the item you choose has changed, please refresh the item page!");
                return "tradeFail";
            }
        }

        orderService.saveOrder(orderInfo);
        return "redirect://payment.gmall.com/index?orderId=";
    }
}
