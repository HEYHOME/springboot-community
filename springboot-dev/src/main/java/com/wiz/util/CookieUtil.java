package com.wiz.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @Description:
 * @Create: 2022-03-19-22:20
 * @Author: Hey
 */
public class CookieUtil {

    public static String getValue(HttpServletRequest request,String name){
        if (request == null || name == null) {
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;

    }

}
