package com.wiz.util;

import com.wiz.entity.User;
import org.springframework.stereotype.Component;

/**
 * @Description: 持有用户信息，用于代替session对象
 * @Create: 2022-03-19-22:32
 * @Author: Hey
 */

@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<User>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
