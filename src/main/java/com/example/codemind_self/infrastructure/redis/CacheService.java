package com.example.codemind_self.infrastructure.redis;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;


import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisService redisService;
    private final RedissonClient redissonClient;


    private final Cache<String,String> localCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private  RBloomFilter<String> bloomFilter;


    @PostConstruct
    private void initBloomFilter(){
        bloomFilter = redissonClient.getBloomFilter("codemind:bloom");
        bloomFilter.tryInit(100000,0.01);
    }

    public void addToBloom(String key){
        bloomFilter.add(key);
    }

    // 判断key是否可能存在
    public boolean mightExist(String key) {
        return bloomFilter.contains(key);
    }


    public String getWithMutiLevel(String key, long redisTtl, Supplier<String> dbLoader ){
        if(!bloomFilter.contains(key)){
            log.debug("布隆过滤器拦截,key不存在：{}",key);
            return null;
        }

        String value = localCache.getIfPresent(key);

        if(value != null){
            return value;
        }

        value = redisService.get(key);
        if(value != null){
            localCache.put(key,value);
            return value;
        }

        value = dbLoader.get();
        if(value != null){
            redisService.set(key,value,redisTtl);
            localCache.put(key,value);
            return value;
        }
        return value;

    }

    public String getWithLock(String key, long redisTtl, Supplier<String> dbLoader){
        String value = localCache.getIfPresent(key);
        if(value != null){
            return value;
        }
        value = redisService.get(key);
        if(value != null){
            localCache.put(key,value);
            return value;
        }

        // 加锁查数据库
        String lockKey = "lock:" + key;
        boolean locked = redisService.tryLock(lockKey,10);
        if(locked){
            try {
                value = redisService.get(key);
                if (value != null) {
                    localCache.put(key, value);
                    return value;
                }
                value = dbLoader.get();
                if (value != null) {
                    redisService.set(key, value, redisTtl);
                    localCache.put(key, value);
                    return value;
                }
                return value;
            }finally {
                redisService.unLock(key);
            }

        }else{
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            value = redisService.get(key);
            if(value != null){
                localCache.put(key,value);
                return value;
            }
            return value;

        }

    }

    // 两级缓存全部删掉
    public void evict(String key){
        localCache.invalidate(key);
        redisService.delete(key);
    }

    public void evictLocal(String key){
        localCache.invalidate(key);
    }

    public void putLocal(String key,String value){
        localCache.put(key,value);
    }

}
