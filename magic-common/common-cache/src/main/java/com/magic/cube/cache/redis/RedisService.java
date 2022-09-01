package com.magic.cube.cache.redis;

import lombok.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: TJ
 * @date: 2022-04-27
 **/
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
public final class RedisService {

    private RedisTemplate redisTemplate;

    public RedisService(RedisTemplate<String, Object> rt) {
        this.redisTemplate = rt;
    }

    /**
     * 设置缓存过期时间
     *
     * @param key  键
     * @param time 过期时间, 单位秒
     */
    public Boolean expire(@NonNull String key, long time) {
        if (time > 0) {
            return redisTemplate.expire(key, time, TimeUnit.SECONDS);
        }
        return false;
    }

    /**
     * 获取指定键过期时间
     *
     * @param key 键
     * @return 过期时间, 单位秒, 返回 0 代表永久有效
     */
    public Long getExpire(@NonNull String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断 key 是否存在
     *
     * @param key 键
     */
    public Boolean hasKey(@NonNull String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除缓存
     */
    public Boolean del(@NonNull String key) {
        return redisTemplate.delete(key);
    }

    // ============================ String

    /**
     * 缓存获取
     */
    public <T> T get(@NonNull String key) {
        ValueOperations<String, T> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    /**
     * 缓存放入
     */
    public void set(@NonNull String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 当key不存在时, set成功, 返回true
     * 当key存在时, 取消set, 返回false
     */
    public Boolean setIfAbsent(@NonNull String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 缓存并设置过期时间
     *
     * @param key   键
     * @param value 值
     * @param time  过期时间, 单位秒; 若小于等于0则永不过期
     */
    public void set(@NonNull String key, Object value, long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加的数量
     */
    public Long incr(@NonNull String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少的数量
     */
    public Long decr(@NonNull String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ================================ Hash

    /**
     * 获取Hash结构缓存值
     *
     * @param key  键
     * @param item 项
     * @return 值
     */
    public Object hGet(@NonNull String key, @NonNull String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取Hash结构缓存值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<String, Object> hGetMap(@NonNull String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Hash结构设置缓存
     *
     * @param key 键
     * @param map 对应多个键值对, key不能为null
     */
    public void hSetMap(@NonNull String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * Hash结构设置缓存带过期时间
     *
     * @param key  键
     * @param map  对应多个键值对, key不能为null
     * @param time 过期时间, 单位秒
     */
    public void hSetMap(@NonNull String key, Map<String, Object> map, long time) {
        redisTemplate.opsForHash().putAll(key, map);
        if (time > 0) {
            expire(key, time);
        }
    }

    /**
     * Hash设置缓存
     */
    public void hSet(@NonNull String key, @NonNull String item, Object value) {
        redisTemplate.opsForHash().put(key, item, value);
    }

    /**
     * Hash设置缓存, 带过期时间
     */
    public void hSet(@NonNull String key, @NonNull String item, Object value, long time) {
        redisTemplate.opsForHash().put(key, item, value);
        if (time > 0) {
            expire(key, time);
        }
    }

    /**
     * Hash删除值
     *
     * @param key  键
     * @param item 项
     */
    public void hDel(@NonNull String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键, 不能为null
     * @param item 项, 不能为null
     */
    public boolean hasHashKey(@NonNull String key, @NonNull String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    // ============================ set

    /**
     * 根据key获取Set中的所有值
     */
    public <T> Set<T> sGet(@NonNull String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断value在set中是否存在
     */
    public Boolean setExist(@NonNull String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值, 可以是多个
     * @return 成功个数
     */
    public Long sSet(@NonNull String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   过期时间, 单位秒
     * @param values 值
     * @return 成功个数
     */
    public Long sSetWithTime(@NonNull String key, long time, Object... values) {
        Long count = redisTemplate.opsForSet().add(key, values);
        if (time > 0L) {
            expire(key, time);
        }
        return count;
    }

    /**
     * 删除值为value的数据
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public Long sDel(@NonNull String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    // =============================== list

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1 代表所有值
     */
    public <T> List<T> lGet(@NonNull String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     */
    public Long lGetSize(@NonNull String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index >= 0时, 0:表头; 1:第二个元素, 依次类推;
     *              index < 0时，-1:表尾; -2:倒数第二个元素, 依次类推
     */
    public Object lGetIndex(@NonNull String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 加入list缓存
     *
     * @param key   键
     * @param value 值
     */
    public void lSet(@NonNull String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 加入list缓存带过期时间
     *
     * @param key   键
     * @param value 值
     * @param time  单位秒
     */
    public void lSet(@NonNull String key, Object value, long time) {
        redisTemplate.opsForList().rightPush(key, value);
        if (time > 0) {
            expire(key, time);
        }
    }

    /**
     * 加入list缓存
     *
     * @param key   键
     * @param value 值
     */
    public void lSet(@NonNull String key, List<Object> value) {
        redisTemplate.opsForList().rightPushAll(key, value);
    }

    /**
     * 加入list缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     */
    public void lSet(@NonNull String key, List<Object> value, long time) {
        redisTemplate.opsForList().rightPushAll(key, value);
        if (time > 0) {
            expire(key, time);
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     */
    public void lUpdateIndex(@NonNull String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    // ================================ zset

    /**
     * zset添加
     */
    public Boolean zAdd(@NonNull String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 获取指定元素分数
     */
    public Double zScore(@NonNull String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * zset获取指定分数内的元素个数
     */
    public Long rangeCount(@NonNull String key, double minScore, double maxScore) {
        return redisTemplate.opsForZSet().count(key, minScore, maxScore);
    }

    /**
     * 获取指定分数范围内的集合
     */
    public Set rangeByScore(@NonNull String key, double minScore, double maxScore) {
        return redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore);
    }

    /**
     * 将元素按score降序之后取 start ~ end 范围内数据;
     * 包含 start/end 索引位置
     */
    public Set reverseRange(@NonNull String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }
}
