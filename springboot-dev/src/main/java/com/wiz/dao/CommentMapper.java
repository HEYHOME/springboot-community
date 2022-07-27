package com.wiz.dao;

import com.wiz.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description:
 * @Create: 2022-03-24-15:08
 * @Author: Hey
 */
@Repository
@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType,int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);

    int selectCountByUserId(int userId, int entityType);

    List<Comment> selectCommentsByUserId(int userId, int entityType, int offset, int limit);

}
