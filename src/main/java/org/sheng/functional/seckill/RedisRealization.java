package org.sheng.functional.seckill;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.sheng.functional.common.RedisPool;
import org.sheng.functional.util.JedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author shengxingyue, created on 2018/1/19
 */
@Slf4j
public class RedisRealization {

    private static final String PRO_NUM_KEY = "proNum";
    private static final int PRO_NUM_VALUE = 10;
    private static final String SUCCESS_LIST_KEY = "successList";
    private static final int PEOPLE_NUM = 100;

    /**
     * 初始化产品数量，成功秒杀列表
     */
    public void initProduct() {
        // 初始化库存量为
        JedisUtil.set(PRO_NUM_KEY, String.valueOf(PRO_NUM_VALUE));
    }

    /**
     * 开始秒杀
     */
    public void startKill() throws InterruptedException {
        // 初始化线程池
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        // 模拟 1000 个人同时开始秒杀
        for (int i = 0; i < PEOPLE_NUM; i++) {
            threadPoolExecutor.execute(new Client(i));
        }

        threadPoolExecutor.shutdown();

        while (true) {
            Thread.sleep(5000);
            if (threadPoolExecutor.isTerminated()) {
                break;
            }
        }
    }

    /**
     * 打印成功结果
     */
    public void printResult() {
        List<String> result = JedisUtil.getList(SUCCESS_LIST_KEY);
        result.forEach(System.out::println);
    }

    /**
     * 模拟用户
     */
    static class Client implements Runnable {
        private int num;

        public Client(int num) {
            this.num = num;
        }

        @Override
        public void run() {
            // 随机睡一会，模拟不同用户的延迟
            try {
                Thread.sleep(RandomUtils.nextInt(1, 100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (true) {
                Jedis jedis = RedisPool.getJedis();
                // 监控库存量
                jedis.watch(PRO_NUM_KEY);
                // 获取当前库存量
                int proNum = Integer.parseInt(jedis.get(PRO_NUM_KEY));

                // 开启事务
                Transaction transaction = jedis.multi();
                // 库存大于 0 则进行下面的逻辑
                if (proNum > 0) {
                    // 更新库存量
                    transaction.set(PRO_NUM_KEY, String.valueOf(--proNum));
                    // 提交事务
                    List<Object> result = transaction.exec();
                    // 提交成功
                    if (!result.isEmpty()) {
                        // 插入成功秒杀用户
                        jedis.lpush(SUCCESS_LIST_KEY, String.valueOf(num));
                        log.info("{} 成功秒杀", num);
                    } else {
                        log.info("并发写失败 {}", num);
                    }
                    RedisPool.retureResource(jedis);
                } else {
                    log.info("库存为空");
                    RedisPool.retureResource(jedis);
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        RedisRealization redisRealization = new RedisRealization();
        redisRealization.initProduct();

        long start = System.currentTimeMillis();
        redisRealization.startKill();
        long end = System.currentTimeMillis();
        log.info(String.format("程序一共运行 %d ms", end - start));

        redisRealization.printResult();
    }
}
