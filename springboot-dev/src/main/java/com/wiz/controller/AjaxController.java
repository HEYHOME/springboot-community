package com.wiz.controller;

import com.wiz.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description:
 * @Create: 2022-03-22-22:58
 * @Author: Hey
 */
@Controller
public class AjaxController {

    @RequestMapping(value = "/ajax",method = RequestMethod.POST)
    @ResponseBody
    public String ajax(String name, int age) {
        /*System.out.println(name);
        System.out.println(age);*/
        return CommunityUtil.getJSONString(1, "操作成功");
    }

}
