package com.example.utils;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class FlowUtils {

    @Resource
    StringRedisTemplate template;

    // 设定60秒只能单次亲求，发送验证码
    public boolean limitOnceCheck(String key, int blockTime){
        if (Boolean.TRUE.equals(template.hasKey(key))){
            return false;   // 包含该键返回false，不能再次发送邮件
        } else {
            template.opsForValue().set(key, "", blockTime, TimeUnit.SECONDS);   // 不包含则存入一个新的key
            return true;
        }
    }
}
