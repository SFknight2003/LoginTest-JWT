package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;

    /**
     * 以下三个方法，用于退出登录操作，将jwt的id存储黑名单之中。
     * @param headerToken
     * @return
     */
    // 是否成功将令牌放入黑名单
    public boolean invalidateJwt(String headerToken){
        String token = this.covertToken(headerToken);
        if(token == null) return false; // token有问题返回false
        // 验证令牌
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            // 将令牌UUID放入黑名单
            DecodedJWT jwt = jwtVerifier.verify(token);
            String id = jwt.getId();    // 取出id
            return deleteToken(id,jwt.getExpiresAt());  // 参数：id，过期时间
        } catch (JWTVerificationException e) {
            return false;
        }
    }
    // jwt放入黑名单
    private boolean deleteToken(String uuid, Date time){        // 过期时间
        // 判断token是否失效
        if( this.isInvalidToken(uuid))
            return false;
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0);   // 查看剩余日期
        // 设置一个键值对存储在redis中。参数：前缀+uuid（键）、空值（值）、过期时间、过期时间单位
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MILLISECONDS);
        return true;
    }
    // 先判断是否是过期令牌
    private boolean isInvalidToken(String uuid){
        // 从redis中进行判断，jwt的id是否存在
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }

    /**
     * 以下内容为解析jwt、token、用户信息
     * @param headerToken
     * @return
     */
    // 解析jwt
    public DecodedJWT resolveJwt(String headerToken){
        String token = this.covertToken(headerToken);
        if(token == null) return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);  // 验证token看是否被篡改
            if(this.isInvalidToken(verify.getId()))
                return null;// 判断jwt是否被拉黑
            Date expiresAt = verify.getExpiresAt(); // 获取过期日期
            return new Date().after(expiresAt) ? null : verify; // 判断是否过期，未过期返回已解析jwt
        } catch (JWTVerificationException e){   // 验证失败返回运行异常
            return null;
        }
    }

    // 创建JWT令牌
    public String createJwt(UserDetails details, int id, String username){
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expireTime();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())    // 将id放入redis，用于验证该令牌
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expire)    // 设定过期时间
                .withIssuedAt(new Date()) // token办法时间
                .sign(algorithm);   // 签名
    }

    // 设定过期时间
    public Date expireTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire * 24 );
        return calendar.getTime();
    }

    // 解析用户信息
    public UserDetails toUser(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("*****")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    // 解析user id
    public Integer toId(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    // 判断token
    private String covertToken(String headerToken){
        if(headerToken == null || !headerToken.startsWith("Bearer"))
            return null;
        return headerToken.substring(7);    // 切割前面7个字符，返回token
    }
}
