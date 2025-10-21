package com.example.springboot.service;

import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
