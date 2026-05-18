package com.dormitory.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtUtil - JWT令牌工具类
 * 
 * 用于生成、解析和验证JWT令牌，包含用户ID、用户类型和用户名等信息
 * 
 * @author 王和友
 * @since 2026
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 获取JWT签名密钥
     * 
     * @return SecretKey 用于JWT签名和验证的密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     * 
     * @param userId 用户ID
     * @param userType 用户类型（1-学生，2-宿管，3-管理员）
     * @param username 用户名
     * @return String 生成的JWT令牌
     */
    public String generateToken(Long userId, Integer userType, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("username", username);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 从令牌中获取JWT声明信息
     * 
     * @param token JWT令牌
     * @return Claims 令牌中的声明信息
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中获取用户ID
     * 
     * @param token JWT令牌
     * @return Long 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.get("userId").toString());
    }

    /**
     * 从令牌中获取用户类型
     * 
     * @param token JWT令牌
     * @return Integer 用户类型
     */
    public Integer getUserType(String token) {
        Claims claims = getClaimsFromToken(token);
        return Integer.parseInt(claims.get("userType").toString());
    }

    /**
     * 从令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return String 用户名
     */
    public String getUsername(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username").toString();
    }

    /**
     * 检查令牌是否过期
     * 
     * @param token JWT令牌
     * @return boolean true表示已过期，false表示未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证令牌是否有效
     * 
     * @param token JWT令牌
     * @return boolean true表示有效，false表示无效或已过期
     */
    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
