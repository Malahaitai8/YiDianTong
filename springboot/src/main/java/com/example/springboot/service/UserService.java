package com.example.springboot.service;

import com.example.springboot.config.SecurityUtils;
import com.example.springboot.entity.User;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

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
    
    /**
     * 修改当前登录用户的密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public void changePassword(String oldPassword, String newPassword) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new CustomerException("401", "未登录");
        }
        
        // 获取当前用户
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new CustomerException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new CustomerException("旧密码不正确");
        }
        
        // 更新密码
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(currentUserId, encodedNewPassword);
    }
}
