package com.tyw.onlineshopping.service;

import com.tyw.onlineshopping.bean.OrderInfo;

public interface OrderService {

    public void saveOrder(OrderInfo orderInfo);

    public String genToken(String user);

    public boolean verifyToken(String userId, String token);
}
