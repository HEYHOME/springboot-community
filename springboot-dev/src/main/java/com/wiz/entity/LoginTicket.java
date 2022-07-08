package com.wiz.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Description:
 * @Create: 2022-03-19-16:17
 * @Author: Hey
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status;
    private Date expired;

}
