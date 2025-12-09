package com.example.springboot.service;

import com.example.springboot.entity.Whitelist;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.WhitelistMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 白名单服务类
 */
@Service
public class WhitelistService {

    @Resource
    private WhitelistMapper whitelistMapper;

    /**
     * 查询所有白名单
     */
    public List<Whitelist> selectAll() {
        return whitelistMapper.selectAll();
    }

    /**
     * 根据ID查询白名单
     */
    public Whitelist selectById(Long id) {
        Whitelist whitelist = whitelistMapper.selectById(id);
        if (whitelist == null) {
            throw new CustomerException("白名单记录不存在");
        }
        return whitelist;
    }

    /**
     * 根据学号/工号查询白名单
     */
    public Whitelist selectByIdentityNumber(String identityNumber) {
        return whitelistMapper.selectByIdentityNumber(identityNumber);
    }

    /**
     * 根据角色类型查询白名单
     */
    public List<Whitelist> selectByRoleType(String roleType) {
        return whitelistMapper.selectByRoleType(roleType);
    }

    /**
     * 添加白名单
     */
    public int create(Whitelist whitelist) {
        // 检查学号/工号是否已存在（查询所有状态）
        Whitelist existing = whitelistMapper.selectByIdentityNumberAnyStatus(whitelist.getIdentityNumber());
        if (existing != null) {
            throw new CustomerException("该学号/工号已存在于白名单中");
        }
        
        if (whitelist.getStatus() == null) {
            whitelist.setStatus("active");
        }
        
        return whitelistMapper.insert(whitelist);
    }

    /**
     * 更新白名单
     */
    public int update(Whitelist whitelist) {
        Whitelist existing = whitelistMapper.selectById(whitelist.getId());
        if (existing == null) {
            throw new CustomerException("白名单记录不存在");
        }
        
        // 如果修改了学号/工号，检查新学号/工号是否已存在（查询所有状态）
        if (whitelist.getIdentityNumber() != null && 
            !whitelist.getIdentityNumber().equals(existing.getIdentityNumber())) {
            Whitelist duplicate = whitelistMapper.selectByIdentityNumberAnyStatus(whitelist.getIdentityNumber());
            if (duplicate != null && !duplicate.getId().equals(whitelist.getId())) {
                throw new CustomerException("该学号/工号已存在于白名单中");
            }
        }
        
        return whitelistMapper.update(whitelist);
    }

    /**
     * 删除白名单
     */
    public int delete(Long id) {
        Whitelist existing = whitelistMapper.selectById(id);
        if (existing == null) {
            throw new CustomerException("白名单记录不存在");
        }
        return whitelistMapper.delete(id);
    }

    /**
     * 批量删除白名单
     */
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomerException("请选择要删除的记录");
        }
        for (Long id : ids) {
            whitelistMapper.delete(id);
        }
    }

    /**
     * 切换白名单状态（启用/停用）
     */
    public void toggleStatus(Long id) {
        Whitelist existing = whitelistMapper.selectById(id);
        if (existing == null) {
            throw new CustomerException("白名单记录不存在");
        }
        
        // 切换状态
        String newStatus = "active".equals(existing.getStatus()) ? "inactive" : "active";
        existing.setStatus(newStatus);
        whitelistMapper.update(existing);
    }
}

