package com.example.codemind_self.infrastructure.redis;



import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


/***
 * 设值，取值，删值，限流校验
 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate restTemplate;
    private final RedisTemplate<Object, Object> redisTemplate;

    public void set(String key,String value,long ttlSeconds){
        restTemplate.opsForValue().set(key,value, ttlSeconds,TimeUnit.SECONDS);
    }

    public String get(String key){
        return restTemplate.opsForValue().get(key);

    }

    public void delete(String key){
        restTemplate.delete(key);
    }

    public boolean isRateLimited(String key, int maxCount, int windowSeconds){
        Long count = restTemplate.opsForValue().increment(key);
        if(count == null){
            return true;
        }
        if(count == 1){
            restTemplate.expire(key,windowSeconds,TimeUnit.SECONDS);
        }
        return count > maxCount;
    }

    public boolean tryLock(String key, long expireSeconds){
        return Boolean.TRUE.equals(restTemplate.opsForValue().setIfAbsent(key,"1",expireSeconds,TimeUnit.SECONDS));
    }

    public void unLock(String key){
        redisTemplate.delete(key);
    }

}
