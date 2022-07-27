package com.wiz.service;

import com.mysql.cj.log.Log;
import com.sun.mail.imap.protocol.UIDSet;
import com.wiz.dao.LoginTicketMapper;
import com.wiz.entity.LoginTicket;
import com.wiz.entity.User;
import com.wiz.dao.UserMapper;
import com.wiz.util.CommunityConstant;
import com.wiz.util.CommunityUtil;
import com.wiz.util.MailClient;
import com.wiz.util.RedisKeyUtil;
import io.lettuce.core.models.role.RedisUpstreamInstance;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.websocket.AsyncChannelWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Create: 2022-03-15-22:04
 * @Author: Hey
 */

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    // @Autowired
    // private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int id) {
        // return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    // 注册账号
    public Map<String, Object> register(User user) {

        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已存在！");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    // 激活账号
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }


    public Map<String, Object> login(String username, String password, int expiredSeconds) {

        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }
        //验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        // loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);


        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket){

        // loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        // return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId, String headerUrl) {
        // return userMapper.updateHeader(userId,headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    public Map<String, Object> updatePassword(int userId, String oldPassword,String newPassword) {
        Map<String,Object> map = new HashMap<>();
        User user = userMapper.selectById(userId);
        String password = CommunityUtil.md5(oldPassword+user.getSalt());
        // 原密码不正确
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "原密码不正确!");
            return map;
        }
        password = CommunityUtil.md5(newPassword+user.getSalt());
        int rows = userMapper.updatePassword(userId, password);
        clearCache(userId);
        return map;
    }

    public Map<String, Object> updatePassword(int userId, String password) {
        Map<String,Object> map = new HashMap<>();
        User user = userMapper.selectById(userId);
        String newPassword = CommunityUtil.md5(password + user.getSalt());
        if (user.getPassword().equals(newPassword)) {
            map.put("passwordMsg","新密码不能与原密码相同");
            return map;
        }
        int rows = userMapper.updatePassword(userId, newPassword);
        clearCache(userId);
        return map;
    }

    // 获取重置密码时的验证码
    public Map<String, Object> getVerifyCode(String email) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        // 验证邮箱
        User user = userMapper.selectByEmail(email);
        if (user == null){
            map.put("emailMsg","该邮箱还未注册过，请注册后再使用！");
            return map;
        }
        //该用户还未激活
        if (user.getStatus() == 0){
            map.put("emailMsg","该邮箱还未激活，请到邮箱中激活后再使用！");
            return map;
        }

        // 邮箱正常情况下,发送验证码
        // 生成验证码
        int num = (int) (Math.random() * 100000);
        String verifycode = num + "";

        // // 验证码的归属
        // String verifyCodeOwner = CommunityUtil.generateUUID();
        // Cookie cookie = new Cookie("verifyCodeOwner", verifyCodeOwner);
        // cookie.setMaxAge(60*5);
        // cookie.setPath((contextPath));
        // response.addCookie(cookie);
        // // 将验证码存入redis
        // String redisKey = RedisKeyUtil.getCodeKey(verifyCodeOwner);
        // redisTemplate.opsForValue().set(redisKey, verifycode, 60 * 5, TimeUnit.SECONDS);

        // 发送邮件
        Context context = new Context();
        context.setVariable("verifycode", verifycode);
        context.setVariable("email", user.getEmail());
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(user.getEmail(), "重置密码",content);
        map.put("verifycode", verifycode);
        map.put("expirationTime", LocalDateTime.now().plusMinutes(5L));//过期时间
        return map;
    }

    // 忘记密码
    public Map<String, Object> forget(String email, String code, String password) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        if (StringUtils.isBlank(code)) {
            map.put("codeMsg","验证码不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        User user = userMapper.selectByEmail(email);
        String newPassword = CommunityUtil.md5(password + user.getSalt());
        if (newPassword.equals(user.getPassword())) {
            map.put("passwordMsg","新密码不能与原密码相同");
            return map;
        }
        userMapper.updatePassword(user.getId(), newPassword);
        return map;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    public User findUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }
    /**
     * 使用Redis缓存用户信息用到的方法
     *
     */
    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据更新时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    // 通过userId获取用户权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}

