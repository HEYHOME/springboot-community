package com.wiz;

import com.wiz.dao.elasticsearch.DiscussPostRepository;
import com.wiz.entity.DiscussPost;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;

/**
 * @Description:
 * @Create: 2022-04-29-14:59
 * @Author: Hey
 */
@SpringBootTest
public class ElasticsearchTest2 {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Test
    public void testSearch(){
        Iterable<DiscussPost> discussPosts = discussPostRepository.findAll();
        Iterator<DiscussPost> iterator = discussPosts.iterator();
        while (iterator.hasNext()) {
            DiscussPost post = iterator.next();
            System.out.println(post);
        }
    }

    @Test
    public void testSearch2(){

    }


}
