package org.sheng.functional.util;

import lombok.extern.slf4j.Slf4j;
import org.sheng.functional.common.RedisPool;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;

/**
 * @author shengxingyue, created on 2018/1/19
 */
@Slf4j
public class JedisUtil {

    public static final String OK = "OK";

    public static List<String> getList(String listName) {
        Jedis jedis = RedisPool.getJedis();
        List<String> result = jedis.lrange(listName, 0, jedis.llen(listName) - 1);
        Collections.reverse(result);
        return result;
    }

    public static String set(String key, String value) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.set(key, value);
        } catch (Exception e) {
            log.error("set key : {}, value : {} error", key, value, e);
            RedisPool.retureBorkenResource(jedis);
            return result;
        }
        RedisPool.retureResource(jedis);
        return result;
    }

    public static String setEx(String key, String value, Integer expireTime) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.setex(key, expireTime, value);
        } catch (Exception e) {
            log.error("set key : {}, value : {} error", key, value, e);
            RedisPool.retureBorkenResource(jedis);
            return result;
        }
        RedisPool.retureResource(jedis);
        return result;
    }

    public static Long resetExpire(String key, Integer expireTime) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.expire(key, expireTime);
        } catch (Exception e) {
            log.error("set key : {}, expire time: {} error", key, expireTime, e);
            RedisPool.retureBorkenResource(jedis);
            return result;
        }
        RedisPool.retureResource(jedis);
        return result;
    }

    public static String get(String key) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("get key : {} error", key, e);
            RedisPool.retureBorkenResource(jedis);
            return result;
        }
        RedisPool.retureResource(jedis);
        return result;
    }

    public static Long del(String key) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del key : {}, error", key, e);
            RedisPool.retureBorkenResource(jedis);
            return result;
        }
        RedisPool.retureResource(jedis);
        return result;
    }
}
