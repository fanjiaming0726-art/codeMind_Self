package com.example.codemind_self.common.constant;

import java.util.Random;

public class RedisConstant {

    /***
     * 设计缓存常量原则：不同key，设置过期时间
     *
     */
    // 限流
    public static final String RATE_LIMIT_PREFIX = "rate_limit";
    public static final int RATE_LIMIT_COUNT = 10;
    public static final int RATE_LIMIT_WINDOW = 60;

    // 对话上下文缓存（每次存的缓存都是当前的所有对话信息，所以每添加一个新缓存都要把旧缓存删掉）
    public static final String CHAT_CONTEXT_PREFIX = "chat_context";
    public static final int CHAT_CONTEXT_TTL = 3600;

    // 问答结果缓存
    public static final String CHAT_CACHE_PREFIX = "chat_cache";
    public static final int CHAT_CACHE_TTL = 86400;

    private RedisConstant(){}

    // 解决缓存雪崩问题，TTL随机偏移范围
    public static final int TTL_RANDOM_RANGE = 3600;

    // 缓存穿透：空值缓存时间
    public static final int NULL_CACHE_TTL = 60;

    // 生成带随机偏移的TTL
    public static int randomTtl(int baseTtl){
        return baseTtl + new Random().nextInt(TTL_RANDOM_RANGE);
    }
}
