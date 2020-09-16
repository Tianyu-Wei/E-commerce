package com.tyw.onlineshopping.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserInfo implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String loginName;

    @Column
    private String nickName;

    @Column
    private String passwd;

    @Column
    private String name;

    @Column
    private String phoneNum;

    @Column
    private String email;

    @Column
    private String headImg;

    @Column
    private String userLevel;
}
