package com.example.springboot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 拦截所有请求，验证JWT token的有效性
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 从请求头中获取token
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // 检查Authorization头是否存在且以"Bearer "开头
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // 移除"Bearer "前缀
            try {
                username = jwtUtil.getUsernameFromToken(token);
            } catch (Exception e) {
                logger.error("JWT解析失败: " + e.getMessage());
            }
        }

        // 如果token有效且当前没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 验证token
            if (jwtUtil.validateToken(token, userDetails)) {
                // 创建认证对象
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 将认证信息存入SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
}

