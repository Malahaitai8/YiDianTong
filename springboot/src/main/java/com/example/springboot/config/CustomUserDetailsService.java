package com.example.springboot.config;

import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义UserDetailsService实现类
 * 从数据库加载用户信息
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 去除用户名首尾空格，避免因空格导致的查询失败
        if (username != null) {
            username = username.trim();
        }
        
        User user = userMapper.selectByUsername(username);
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        return new CustomUserDetails(user);
    }
}

