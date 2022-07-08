package com.wiz.entity;


import lombok.Data;

import java.util.Date;

/**
 * @Description:
 * @Create: 2022-03-24-15:05
 * @Author: Hey
 */

@Data
public class Comment {

    private int id;
    private int userId;
    private int entityType;
    private int entityId;
    private int targetId;
    private String content;
    private int status;
    private Date createTime;

}
