package com.wiz.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Description:
 * @Create: 2022-03-14-21:49
 * @Author: Hey
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;  // 0-普通用户; 1-超级管理员; 2-版主;
    private int status;  // 0-未激活; 1-已激活;
    private String activationCode;
    private String headerUrl;
    private Date createTime;
}
