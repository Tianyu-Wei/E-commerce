package com.tyw.onlineshopping.service;

import com.tyw.onlineshopping.bean.UserAddress;
import com.tyw.onlineshopping.bean.UserInfo;

import java.util.List;

public interface UserService {

    List<UserInfo> getUserInfoListAll();

    void addUser(UserInfo userInfo);

    void updateUser(UserInfo userInfo);

    void updateUserByName(String name, UserInfo userInfo);

    void delUser(UserInfo userInfo);

    UserInfo getUserById(String id);

    UserInfo login(UserInfo userInfo);

    Boolean verify(String userId);

    public List<UserAddress> getUserAddressList(String userId);
}
