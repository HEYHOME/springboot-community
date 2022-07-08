package com.wiz;

import com.wiz.dao.DiscussPostMapper;
import com.wiz.dao.LoginTicketMapper;
import com.wiz.dao.MessageMapper;
import com.wiz.entity.DiscussPost;
import com.wiz.entity.LoginTicket;
import com.wiz.entity.Message;
import com.wiz.entity.User;
import com.wiz.dao.UserMapper;
import com.wiz.util.MailClient;
import com.wiz.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.util.HtmlUtils;

import javax.sql.DataSource;
import java.util.Date;
import java.sql.SQLException;
import java.util.List;

@SpringBootTest
class SpringbootDevApplicationTests {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private MailClient mailClient;

    @Qualifier("dataSource")
    @Autowired
    private DataSource dataSource;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test() {
        User user = userMapper.selectById(102);
        System.out.println(user);
    }

    @Test
    public void test2() throws SQLException {
        System.out.println(dataSource.getConnection());
        System.out.println(dataSource.getClass());
    }

    @Test
    public void insert() {
        User user = new User();
        user.setUsername("test1");
        user.setPassword("1234567");
        user.setSalt("abcd");
        user.setEmail("test1@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void update() {
        int rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "helloworld");
        System.out.println(rows);

        rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testSendMail() {

        mailClient.sendMail("1414626899@qq.com", "Test", "hello world!");
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testSensitiveWords() {
        String text = "这里不可以赌博,不可以嫖娼,不可以开票，哈哈";
        String filterText = sensitiveFilter.filter(text);
        System.out.println(filterText);
    }

    @Test
    public void testSelectLetters() {
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message msg : messages) {
            System.out.println(msg);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        List<Message> messages1 = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : messages1) {
            System.out.println(message);
        }

        int count1 = messageMapper.selectLetterCount("111_112");
        System.out.println(count1);

        int count2 = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count2);
    }

    @Test
    public void testRedis(){
        String key = "test:count";
        redisTemplate.opsForValue().set(key,1);
        System.out.println(redisTemplate.opsForValue().get(key));
    }

    @Test
    public void testHtmlEscape(){
        String target = "<p>123</p>";
        String s1 = HtmlUtils.htmlEscape(target);
        System.out.println(s1);
        System.out.println(target);
    }
}
