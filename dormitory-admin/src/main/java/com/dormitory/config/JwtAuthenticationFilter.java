package com.dormitory.config;

import com.dormitory.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JwtAuthenticationFilter - JWT认证过滤器
 * 
 * 用于拦截所有HTTP请求，解析请求头中的JWT令牌，
 * 验证令牌有效性并设置Spring Security上下文，实现无状态认证
 * 
 * @author 王和友
 * @since 2026
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 处理每个请求的JWT验证
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                   @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (token != null && jwtUtil.validateToken(token)) {
            try {
                Long userId = jwtUtil.getUserId(token);
                Integer userType = jwtUtil.getUserType(token);

                String role = "ROLE_USER";
                if (userType == 2) {
                    role = "ROLE_MANAGER";
                } else if (userType == 3) {
                    role = "ROLE_ADMIN";
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority(role))
                        );

                authentication.setDetails(userType);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                logger.error("JWT解析失败", e);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取JWT令牌
     * 
     * @param request HTTP请求
     * @return String JWT令牌（不含Bearer前缀），不存在则返回null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
