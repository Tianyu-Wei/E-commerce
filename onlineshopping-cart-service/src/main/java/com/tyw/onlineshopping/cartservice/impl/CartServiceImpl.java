package com.tyw.onlineshopping.cartservice.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.tyw.onlineshopping.bean.CartInfo;
import com.tyw.onlineshopping.bean.SkuInfo;
import com.tyw.onlineshopping.cartservice.mapper.CartInfoMapper;
import com.tyw.onlineshopping.service.CartService;
import com.tyw.onlineshopping.service.ManageService;
import com.tyw.onlineshopping.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageService manageService;

    @Override
    public CartInfo addCart(String userId, String skuId, Integer num) throws InterruptedException {
        loadCartCacheIfNotExists(userId);

        CartInfo cartInfoQuery = new CartInfo();
        cartInfoQuery.setSkuId(skuId);
        cartInfoQuery.setUserId(userId);
        CartInfo cartInfoExists = null;
        cartInfoExists = cartInfoMapper.selectOne(cartInfoQuery);
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        if (cartInfoExists != null) {
            cartInfoExists.setSkuName(skuInfo.getSkuName());
            cartInfoExists.setCartPrice(skuInfo.getPrice());
            cartInfoExists.setSkuNum(cartInfoExists.getSkuNum() + num);
            cartInfoExists.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExists);
        } else {
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(num);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfoMapper.insertSelective(cartInfo);
            cartInfoExists = cartInfo;
        }

        loadCartCache(userId);
//
//        Jedis jedis = redisUtil.getJedis();
//        //add cache
//        String cartKey = "cart:" + userId + ":info";
//        String cartInfoJson = JSON.toJSONString(cartInfoExists);
//        jedis.hset(cartKey, userId, cartInfoJson); //add new
//
//        jedis.close();
        //Load database
        return cartInfoExists;

    }

    /**
     * No data in the cache, check database
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> cartList(String userId) {
        //check cache
        Jedis jedis = redisUtil.getJedis();
        String cartKey = "cart:" + userId + ":info";
        List<String> cartJsonList = jedis.hvals(cartKey);
        List<CartInfo> cartList = new ArrayList<>();

        //Cache hit
        if (cartJsonList != null && cartJsonList.size() > 0) {
            for (String s : cartJsonList) {
                CartInfo cartInfo = JSON.parseObject(s, CartInfo.class);
                cartList.add(cartInfo);
            }
            cartList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o2.getId().compareTo(o1.getId());
                }
            });
            return cartList;
        } else {
            //Not hit cache
           return loadCartCache(userId);
        }
    }

    public List<CartInfo> loadCartCache(String userId) {
        //Read database
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithSkuPrice(userId);
        if (cartInfoList!=null&&cartInfoList.size() > 0) {
            Map<String, String> cartMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                cartMap.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
            }

            //Write into database

            Jedis jedis = redisUtil.getJedis();
            String cartKey = "cart:" + userId + ":info";
            jedis.del(cartKey);
            jedis.hmset(cartKey, cartMap);
            jedis.expire(cartKey, 60 * 60 * 24);
            jedis.close();
        }

        return cartInfoList;
    }

    /**
     * Merge cart
     * @param userIdDest
     * @param userIdOrig
     * @return
     */
    @Override
    public List<CartInfo> mergeCartList(String userIdDest, String userIdOrig) {
        //1 Merge
        cartInfoMapper.mergeCartList(userIdDest, userIdOrig);
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userIdOrig);
        cartInfoMapper.delete(cartInfo);

        List<CartInfo> cartInfoList = loadCartCache(userIdDest);

        return cartInfoList;
    }

    public void loadCartCacheIfNotExists (String userId) {
        String cartKey = "cart:" + userId + ":info";
        Jedis jedis = redisUtil.getJedis();
        Long ttl = jedis.ttl(cartKey);
        int ttlInt = ttl.intValue();
        jedis.expire(cartKey, 10+ttlInt);
        Boolean exists = jedis.exists(cartKey);
        jedis.close();;
        if (!exists) {
            loadCartCache(userId);
        }
    }

    @Override
    public void checkCart(String userId, String skuId, String isChecked) {
        loadCartCacheIfNotExists(userId); //Check whether cache exists, to be avoid cache expired

        String cartKey = "cart:" + userId + ":info";
        Jedis jedis = redisUtil.getJedis();
        String cartInfoJson = jedis.hget(cartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);

        cartInfo.setIsChecked(isChecked);
        String cartInfoJsonNew = JSON.toJSONString(cartInfo);
        jedis.hset(cartKey, skuId, cartInfoJsonNew);

        //For checking out, put the checked items into a seperate place.
        String cartCheckedKey = "cart:" + userId + ":info";
        if (isChecked.equals("1")) {  //Add into awaiting cart list with the checked items
            jedis.hset(cartCheckedKey, skuId, cartInfoJsonNew);
            jedis.expire(cartCheckedKey, 60*60);
        } else {
            jedis.hdel(cartCheckedKey, skuId);
        }
        jedis.close();
    }

    @Override
    public List<CartInfo> getCheckedCartList(String userId) {
        String cartCheckedKey = "cart:" + userId + ":info";
        Jedis jedis = redisUtil.getJedis();

        List<String> checkedCartList = jedis.hvals(cartCheckedKey);
        List<CartInfo> cartInfoList = new ArrayList<>();
        for (String cartInfoJson: checkedCartList) {
            CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
            cartInfoList.add(cartInfo);
        }

        jedis.close();

        return cartInfoList;
    }
}
