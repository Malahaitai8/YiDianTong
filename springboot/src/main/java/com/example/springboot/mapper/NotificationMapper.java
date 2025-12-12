package com.example.springboot.mapper;

import com.example.springboot.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface NotificationMapper {

    int insert(Notification notification);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("time") Date time,
                     @Param("errorMessage") String errorMessage);

    List<Notification> selectByUserId(@Param("userId") Long userId,
                                      @Param("limit") Integer limit);

    Long countUnreadByUserId(@Param("userId") Long userId);

    int markAsRead(@Param("id") Long id, @Param("readTime") Date readTime);

    int markAllAsRead(@Param("userId") Long userId, @Param("readTime") Date readTime);
}

































