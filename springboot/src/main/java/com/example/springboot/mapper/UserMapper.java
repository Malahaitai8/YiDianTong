package com.example.springboot.mapper;

import com.example.springboot.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User selectById(Long id);
    User selectByUsername(String username);
    int insert(User user);
    int update(User user);
    int deleteById(Long id);
    List<User> listByRoleAndStatus(@Param("role") String role, @Param("status") String status);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    int updateAllPasswords(@Param("password") String password);
}
