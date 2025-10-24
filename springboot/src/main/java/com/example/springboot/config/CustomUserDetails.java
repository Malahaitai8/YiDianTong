package com.example.springboot.config;

import com.example.springboot.constants.RoleConstants;
import com.example.springboot.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 自定义UserDetails实现类
 * 包装User实体，用于Spring Security认证
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回用户角色，使用统一的角色转换方法
        return Collections.singletonList(
                new SimpleGrantedAuthority(RoleConstants.toSpringRole(user.getRole()))
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 只有active状态的用户才能登录
        return "active".equals(user.getStatus());
    }

    /**
     * 获取原始User对象
     */
    public User getUser() {
        return user;
    }
}

