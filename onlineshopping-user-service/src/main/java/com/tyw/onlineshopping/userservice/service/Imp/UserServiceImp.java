package com.tyw.onlineshopping.userservice.service.Imp;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.tyw.onlineshopping.bean.UserAddress;
import com.tyw.onlineshopping.bean.UserInfo;
import com.tyw.onlineshopping.userservice.mapper.UserAddressMapper;
import com.tyw.onlineshopping.userservice.mapper.UserMapper;
import com.tyw.onlineshopping.service.UserService;
import com.tyw.onlineshopping.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImp implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UserInfo> getUserInfoListAll() {
        List<UserInfo> userInfoList = userMapper.selectAll();
        return userInfoList;
    }

    @Override
    public void addUser(UserInfo userInfo) {
        String passwd = userInfo.getPasswd();
        String encriptedPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(encriptedPasswd);

        userMapper.insertSelective(userInfo);
    }

    @Override
    public void updateUser(UserInfo userInfo) {
        userMapper.updateByPrimaryKeySelective(userInfo);
    }

    @Override
    public void updateUserByName(String name, UserInfo userInfo) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name", name);

        userMapper.updateByExampleSelective(userInfo, example);
    }

    @Override
    public void delUser(UserInfo userInfo) {
        userMapper.delete(userInfo);
    }

    @Override
    public UserInfo getUserById(String id) {
        return userMapper.selectByPrimaryKey(id);
    }

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;


    @Override
    public UserInfo login(UserInfo userInfo) {
        // 1  比对数据库  用户名和密码
        String passwd = userInfo.getPasswd();
        String passwdMd5 = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(passwdMd5);

        UserInfo userInfoExists = userMapper.selectOne(userInfo);

        if(userInfoExists!=null){
            // 2 加载缓存
            Jedis jedis = redisUtil.getJedis();
            //   type String     key  user:1011:info       value    userInfoJson
            String userKey=userKey_prefix+userInfoExists.getId()+userinfoKey_suffix;
            String userInfoJson = JSON.toJSONString(userInfoExists);
            jedis.setex(userKey,userKey_timeOut,userInfoJson);
            jedis.close();
            return  userInfoExists;
        }

        return null ;
    }

    @Override
    public Boolean verify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String userKey=userKey_prefix+userId+userinfoKey_suffix;
        Boolean isLogin = jedis.exists(userKey);
        if(isLogin){  //如果经过验证，延长用户使用时间
            jedis.expire(userKey,userKey_timeOut);
        }

        jedis.close();

        return isLogin;
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> userAddresses = userAddressMapper.select(userAddress);
        return userAddresses;
    }


}
