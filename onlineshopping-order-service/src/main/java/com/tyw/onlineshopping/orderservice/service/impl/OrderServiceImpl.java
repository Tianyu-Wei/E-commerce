package com.tyw.onlineshopping.orderservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.tyw.onlineshopping.bean.OrderDetail;
import com.tyw.onlineshopping.bean.OrderInfo;
import com.tyw.onlineshopping.orderservice.mapper.OrderDetailMapper;
import com.tyw.onlineshopping.orderservice.mapper.OrderInfoMapper;
import com.tyw.onlineshopping.service.OrderService;
import com.tyw.onlineshopping.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Override
    @Transactional
    public void saveOrder(OrderInfo orderInfo) {
        orderInfoMapper.insertSelective(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
    }

    @Override
    public String genToken(String userId) {
        //Token type key value
        String token = UUID.randomUUID().toString();
        String tokenKey = "user:" + userId + ":trade_code";
        Jedis jedis = redisUtil.getJedis();
        jedis.setex(tokenKey, 10*60, token);
        jedis.close();

        return token;
    }

    @Override
    public boolean verifyToken(String userId, String token) {
        String tokenKey = "user:" + userId + ":trade_code";
        Jedis jedis = redisUtil.getJedis();
        String tokenExists = jedis.get(tokenKey);
        jedis.watch(tokenKey);
        Transaction multi = jedis.multi();

        if(tokenExists!= null && tokenExists.equals(token)) {
            multi.del(tokenKey);
        }
        List<Object> list = multi.exec();
        if (list != null && list.size() >0 && (Long)list.get(0) == 1L) {
            return true;
        }else{
            return false;
        }
    }
}
