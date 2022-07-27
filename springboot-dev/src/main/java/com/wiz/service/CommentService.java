package com.wiz.service;

import com.wiz.dao.CommentMapper;
import com.wiz.entity.Comment;
import com.wiz.util.CommunityConstant;
import com.wiz.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @Description:
 * @Create: 2022-03-24-15:20
 * @Author: Hey
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 该方法有两个表操作，为了保证数据的一致性，对其进行事务管理
     * @param comment
     * @return int
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);
        // 更新帖子的评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            // 评论帖子的评论id对应的是帖子的id，评论评论的评论id对应的是评论的id
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }

        return rows;
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    public int findCountByUserId(int userId, int entityType) {
        return commentMapper.selectCountByUserId(userId, entityType);
    }

    public List<Comment> findCommentByUserId(int userId, int entityType, int offset, int limit) {
        return commentMapper.selectCommentsByUserId(userId, entityType,offset, limit);
    }
}
