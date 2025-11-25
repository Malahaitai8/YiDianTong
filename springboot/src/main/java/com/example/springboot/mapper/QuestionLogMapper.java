package com.example.springboot.mapper;

import com.example.springboot.entity.QuestionLog;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface QuestionLogMapper {

    /**
     * 插入问题记录
     */
    int insert(QuestionLog questionLog);

    /**
     * 获取最近的问题记录（用于高频分析）
     */
    List<String> findRecentQuestions(@Param("limit") int limit);

    /**
     * 根据用户ID查询问题记录
     */
    List<QuestionLog> findByUserId(@Param("userId") Long userId);

    /**
     * 统计某个问题出现的次数
     */
    int countByQuestionContent(@Param("questionContent") String questionContent);

    /**
     * 获取所有问题记录（分页）
     */
    List<QuestionLog> findAll(@Param("offset") int offset, @Param("limit") int limit);
}