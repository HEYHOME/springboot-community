package com.wiz.controller;

import com.github.mustachejava.reflect.guards.MapGuard;
import com.wiz.annotation.LoginRequired;
import com.wiz.entity.Comment;
import com.wiz.entity.DiscussPost;
import com.wiz.entity.Page;
import com.wiz.entity.User;
import com.wiz.service.*;
import com.wiz.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.jni.Local;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.PartHttpMessageWriter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Create: 2022-03-20-21:41
 * @Author: Hey
 */

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LIkeService lIkeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;


    @LoginRequired
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "site/setting";
    }

    @LoginRequired
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片");
            return "site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确");
            return "site/setting";
        }

        // 生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务区发生异常", e);
        }

        // 更新当前用户的头像的路径（web访问路径）
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(value = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {

        // 服务器存放路径
        filename = uploadPath + "/" + filename;
        //文件的后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filename);
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            LOGGER.error("读取头像失败：" + e.getMessage());
        }

    }

    //修改密码
    @RequestMapping(path = "/password", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map.containsKey("passwordMsg")) {
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/setting";
        }
        return "redirect:/index";
    }

    // 获取重置密码时的验证码
    @RequestMapping(path = "/getcode", method = RequestMethod.GET)
    public String getVerifyCode(String email, Model model, HttpSession session) {
        System.out.println("===================getVerifyCode");
        Map<String, Object> map = userService.getVerifyCode(email);
        if (map.containsKey("verifycode")) {  // 正常情况
            session.setAttribute("code", map.get("verifycode"));
            session.setAttribute("expirationTime", map.get("expirationTime"));
        } else {  // 错误情况
            model.addAttribute("emailMsg", map.get("emailMsg"));
        }
        return "/site/forget";
    }

    // 忘记密码
    @RequestMapping(path = "/forget", method = RequestMethod.POST)
    public String forget(String email, String code, String password, Model model, HttpSession session) {
        if (!code.equals(session.getAttribute("code"))) {
            model.addAttribute("codeMsg", "输入的验证码不正确");
            return "/site/forget";
        }
        if (LocalDateTime.now().isAfter((LocalDateTime) session.getAttribute("expirationTime"))) {
            model.addAttribute("codeMsg", "验证码已过期,请重新获取验证码");
            return "/site/forget";
        }
        Map<String, Object> map = userService.forget(email, code, password);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "密码修改成功，可以使用新密码登录了!");
            model.addAttribute("target", "/login");
            return "site/operate-result";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("codeMsg", map.get("codeMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }

    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = lIkeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "site/profile";
    }

    // 我的帖子
    @RequestMapping(path = "/mypost/{userId}", method = RequestMethod.GET)
    public String getMyPostPage(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        int rows = discussPostService.findDiscussPostRows(userId);
        page.setRows(rows);
        page.setPath("/user/mypost/" + userId);
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(),0);
        List<Map<String, Object>> list = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost post : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                long likeCount = lIkeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                list.add(map);
            }
        }
        model.addAttribute("user", user);
        model.addAttribute("discussPosts", list);
        model.addAttribute("discussPostRows", rows);
        return "/site/my-post";
    }

    // 我的评论
    @RequestMapping(path = "/myreply/{userId}", method = RequestMethod.GET)
    public String getMyReplyPage(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        int commentCount = commentService.findCountByUserId(userId, ENTITY_TYPE_POST);
        page.setRows(commentCount);
        page.setPath("/user/myreply/" + userId);
        List<Comment> list = commentService.findCommentByUserId(userId, ENTITY_TYPE_POST, page.getOffset(), page.getLimit());
        List<Map<String, Object>> comments = new ArrayList<>();
        if (list != null) {
            for (Comment comment : list) {
                Map<String,Object> map = new HashMap<>();
                map.put("comment", comment);
                DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("post",post);
                comments.add(map);
            }
        }
        model.addAttribute("comments",comments);
        model.addAttribute("commentCount",commentCount);
        model.addAttribute("user",user);
        return "/site/my-reply";
    }

}



















