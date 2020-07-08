package com.sparksys.commons.cache.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.sparksys.commons.core.cache.ICacheAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * description：Guava Cache
 *
 * @author zhouxinlei
 * @date 2020/6/17 0017
 */
@Slf4j
@Component
public class GuavaCacheAdapter implements ICacheAdapter {

    private static final Map<String, Cache<String, Object>> CACHE_CONCURRENT_MAP = Maps.newConcurrentMap();

    private static final long CACHE_MAXIMUM_SIZE = 100;

    private static final long CACHE_MINUTE = 10 * 60;

    private static final Lock LOCK = new ReentrantLock();

    static {
        Cache<String, Object> cacheContainer = CacheBuilder.newBuilder()
                .maximumSize(CACHE_MAXIMUM_SIZE)
                //最后一次写入后的一段时间移出
                .expireAfterWrite(CACHE_MINUTE, TimeUnit.SECONDS)
                //.expireAfterAccess(CACHE_MINUTE, TimeUnit.MILLISECONDS) //最后一次访问后的一段时间移出
                .recordStats()//开启统计功能
                .build();
        CACHE_CONCURRENT_MAP.put(String.valueOf(CACHE_MINUTE), cacheContainer);
    }

    /**
     * 查询缓存
     *
     * @param key 缓存键 不可为空
     **/
    @Override
    public <T> T get(String key) {
        return get(key, null, null, CACHE_MINUTE);
    }

    /**
     * 查询缓存
     *
     * @param key      缓存键 不可为空
     * @param function 如没有缓存，调用该callable函数返回对象 可为空
     **/
    @Override
    public <T> T get(String key, Function<String, T> function) {
        return get(key, function, key, CACHE_MINUTE);
    }

    /**
     * 查询缓存
     *
     * @param key       缓存键 不可为空
     * @param function  如没有缓存，调用该callable函数返回对象 可为空
     * @param funcParam function函数的调用参数
     **/
    @Override
    public <T, M> T get(String key, Function<M, T> function, M funcParam) {
        return get(key, function, funcParam, CACHE_MINUTE);
    }

    /**
     * 查询缓存
     *
     * @param key        缓存键 不可为空
     * @param function   如没有缓存，调用该callable函数返回对象 可为空
     * @param expireTime 过期时间（单位：毫秒） 可为空
     **/
    @Override
    public <T> T get(String key, Function<String, T> function, Long expireTime) {
        return get(key, function, key, expireTime);
    }

    /**
     * 查询缓存
     *
     * @param key        缓存键 不可为空
     * @param function   如没有缓存，调用该callable函数返回对象 可为空
     * @param funcParam  function函数的调用参数
     * @param expireTime 过期时间（单位：毫秒） 可为空
     **/
    @Override
    public <T, M> T get(String key, Function<M, T> function, M funcParam, Long expireTime) {
        T obj = null;
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        expireTime = getExpireTime(expireTime);
        Cache<String, Object> cacheContainer = getCacheContainer(expireTime);
        try {
            if (function == null) {
                obj = (T) cacheContainer.getIfPresent(key);
            } else {
                obj = (T) cacheContainer.get(key, () -> function.apply(funcParam));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return obj;
    }

    /**
     * 设置缓存键值  直接向缓存中插入值，这会直接覆盖掉给定键之前映射的值
     *
     * @param key 缓存键 不可为空
     * @param obj 缓存值 不可为空
     **/
    @Override
    public <T> void set(String key, T obj) {
        set(key, obj, CACHE_MINUTE);
    }

    /**
     * 设置缓存键值  直接向缓存中插入值，这会直接覆盖掉给定键之前映射的值
     *
     * @param key        缓存键 不可为空
     * @param obj        缓存值 不可为空
     * @param expireTime 过期时间（单位：毫秒） 可为空
     **/
    @Override
    public <T> void set(String key, T obj, Long expireTime) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        if (obj == null) {
            return;
        }
        expireTime = getExpireTime(expireTime);
        Cache<String, Object> cacheContainer = getCacheContainer(expireTime);
        cacheContainer.put(key, obj);
    }

    @Override
    public void expire(String key, Long expireTime) {

    }

    @Override
    public Long increment(String key, long delta) {
        return null;
    }

    @Override
    public Long decrement(String key, long delta) {
        return null;
    }

    /**
     * 移除缓存
     *
     * @param key 缓存键 不可为空
     **/
    @Override
    public void remove(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        long expireTime = getExpireTime(CACHE_MINUTE);
        Cache<String, Object> cacheContainer = getCacheContainer(expireTime);
        cacheContainer.invalidate(key);
    }

    /**
     * 是否存在缓存
     *
     * @param key 缓存键 不可为空
     **/
    @Override
    public boolean contains(String key) {
        boolean exists = false;
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        Object obj = get(key);
        if (obj != null) {
            exists = true;
        }
        return exists;
    }

    @Override
    public <T> Boolean setZSet(String key, Long score, T value) {
        return null;
    }

    @Override
    public <T> Long get(String key, T value) {
        return null;
    }

    @Override
    public <T> T get(String key, Long score) {
        return null;
    }

    @Override
    public <K, V> void setHash(String key, K hashKey, V value) {

    }

    @Override
    public <K> void removeHashEntity(String key, K hashKey) {

    }

    @Override
    public Map getHash(String key) {
        return null;
    }


    private Cache<String, Object> getCacheContainer(Long expireTime) {
        Cache<String, Object> cacheContainer;
        if (expireTime == null) {
            return null;
        }
        String mapKey = String.valueOf(expireTime);
        if (CACHE_CONCURRENT_MAP.containsKey(mapKey)) {
            cacheContainer = CACHE_CONCURRENT_MAP.get(mapKey);
            return cacheContainer;
        }
        LOCK.lock();
        try {
            cacheContainer = CacheBuilder.newBuilder()
                    .maximumSize(CACHE_MAXIMUM_SIZE)
                    //最后一次写入后的一段时间移出
                    .expireAfterWrite(expireTime, TimeUnit.SECONDS)
                    //.expireAfterAccess(AppConst.CACHE_MINUTE, TimeUnit.MILLISECONDS) //最后一次访问后的一段时间移出
                    .recordStats()//开启统计功能
                    .build();
            CACHE_CONCURRENT_MAP.put(mapKey, cacheContainer);
        } finally {
            LOCK.unlock();
        }
        return cacheContainer;
    }

    /**
     * 获取过期时间 单位：秒
     *
     * @param expireTime 传人的过期时间 单位秒 如小于1分钟，默认为10分钟
     **/
    private Long getExpireTime(Long expireTime) {
        Long result = expireTime;
        if (expireTime == null || expireTime < CACHE_MINUTE) {
            result = CACHE_MINUTE;
        }
        return result;
    }

}
