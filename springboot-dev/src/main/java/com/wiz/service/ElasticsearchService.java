package com.wiz.service;

import com.wiz.dao.elasticsearch.DiscussPostRepository;
import com.wiz.entity.DiscussPost;
import org.elasticsearch.client.eql.EqlSearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Create: 2022-04-29-18:01
 * @Author: Hey
 */
@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public void saveDiscussPost(DiscussPost post) {
        discussPostRepository.save(post);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    public List<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        // 构造查询条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        SearchHits<DiscussPost> hits = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        if(hits.getTotalHits() <= 0) {
            return null;
        }

        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit<DiscussPost> hit : hits) {
            DiscussPost post = new DiscussPost();
            int id = hit.getContent().getId();
            post.setId(id);

            int userId = hit.getContent().getUserId();
            post.setUserId(userId);

            int status = hit.getContent().getStatus();
            post.setStatus(status);

            Date createTime = hit.getContent().getCreateTime();
            post.setCreateTime(createTime);

            int commentCount = hit.getContent().getCommentCount();
            post.setCommentCount(commentCount);

            //将高亮的内容填充到title,content中
            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            post.setTitle(highlightFields.get("title")==null ? hit.getContent().getTitle():highlightFields.get("title").get(0));
            post.setContent(highlightFields.get("content")==null ? hit.getContent().getContent():highlightFields.get("content").get(0));

            list.add(post);
        }
        
        return list;
    }

    public long findDiscussPostCount(String keyword,int current, int limit) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        SearchHits<DiscussPost> hits = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        if(hits.getTotalHits() <= 0) {
            return 0;
        }
        return hits.getTotalHits();
    }
}
