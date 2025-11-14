package com.example.springboot.mapper;

import com.example.springboot.entity.Whitelist;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 白名单Mapper接口
 */
@Mapper
public interface WhitelistMapper {
    /**
     * 查询所有白名单
     */
    List<Whitelist> selectAll();
    
    /**
     * 根据ID查询白名单
     */
    Whitelist selectById(Long id);
    
    /**
     * 根据学号/工号查询白名单（仅查询active状态）
     */
    Whitelist selectByIdentityNumber(String identityNumber);
    
    /**
     * 根据学号/工号查询白名单（查询所有状态，用于检查是否存在）
     */
    Whitelist selectByIdentityNumberAnyStatus(String identityNumber);
    
    /**
     * 根据角色类型查询白名单
     */
    List<Whitelist> selectByRoleType(String roleType);
    
    /**
     * 添加白名单
     */
    int insert(Whitelist whitelist);
    
    /**
     * 更新白名单
     */
    int update(Whitelist whitelist);
    
    /**
     * 删除白名单
     */
    int delete(Long id);
}

