package com.example.springboot.config;

import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 密码加密工具类
 * 用于初始化时将数据库中的明文密码加密
 * 
 * 注意：这个类只在首次初始化时运行一次，之后应该注释掉@Component注解
 */
//@Component  // 首次运行时取消注释，加密完成后重新注释掉
public class PasswordEncoderUtil implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始加密用户密码...");
        
        // 这里需要实现查询所有用户的方法
        // List<User> users = userMapper.selectAll();
        
        // for (User user : users) {
        //     // 检查密码是否已加密（BCrypt加密后的密码以$2a$开头）
        //     if (!user.getPassword().startsWith("$2a$")) {
        //         String encodedPassword = passwordEncoder.encode(user.getPassword());
        //         user.setPassword(encodedPassword);
        //         userMapper.update(user);
        //         System.out.println("已加密用户: " + user.getUsername());
        //     }
        // }
        
        System.out.println("密码加密完成！");
    }
}

