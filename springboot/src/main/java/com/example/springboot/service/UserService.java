package com.example.springboot.service;

import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    public int create(User user) {
        return userMapper.insert(user);
    }

    public int update(User user) {
        return userMapper.update(user);
    }

    public int deleteById(Long id) {
        return userMapper.deleteById(id);
    }

    /**
     * 获取待审核的用户列表（按角色筛选）
     * @param role 用户角色（如 "DOCTOR"）
     * @return 待审核用户列表
     */
    public List<User> getPendingApprovalUsers(String role) {
        return userMapper.listByRoleAndStatus(role, "pending_approval");
    }

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 新状态（active/inactive/pending_approval）
     * @return 影响行数
     */
    public int updateUserStatus(Long userId, String status) {
        return userMapper.updateStatus(userId, status);
    }

    /**
     * 审核通过 - 将用户状态改为active
     * @param userId 用户ID
     * @return 影响行数
     */
    public int approveUser(Long userId) {
        return userMapper.updateStatus(userId, "active");
    }

    /**
     * 审核拒绝 - 将用户状态改为inactive
     * @param userId 用户ID
     * @return 影响行数
     */
    public int rejectUser(Long userId) {
        return userMapper.updateStatus(userId, "inactive");
    }
}
