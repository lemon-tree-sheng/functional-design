package org.sheng.functional.common;

import lombok.extern.slf4j.Slf4j;
import org.sheng.functional.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author shengxingyue, created on 2018/1/19
 */
@Slf4j
public class RedisPool {
    private static JedisPool jedisPool;
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total"));
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle"));
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle"));
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.on.borrow"));
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.on.return"));
    private static String redisHost = PropertiesUtil.getProperty("redis.host");
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));

    /**
     * init jedis pool
     */
    private static void initPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        jedisPoolConfig.setTestOnReturn(testOnReturn);
        jedisPoolConfig.setBlockWhenExhausted(true);

        jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort, 1000 * 2);
    }

    static {
        initPool();
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    public static void retureResource(Jedis jedis) {
        jedisPool.returnResource(jedis);
    }

    public static void retureBorkenResource(Jedis jedis) {
        jedisPool.returnBrokenResource(jedis);
    }
}
