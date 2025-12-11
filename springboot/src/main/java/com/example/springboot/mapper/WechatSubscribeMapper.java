package com.example.springboot.mapper;

import com.example.springboot.entity.WechatSubscribe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface WechatSubscribeMapper {

    int insert(WechatSubscribe wechatSubscribe);

    WechatSubscribe selectValidAuth(@Param("userId") Long userId,
                                   @Param("templateId") String templateId,
                                   @Param("now") Date now);

    List<WechatSubscribe> selectByUserId(@Param("userId") Long userId);
}































