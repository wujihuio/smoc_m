package com.smoc.cloud.http.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class IdGeneratorFactory {

    public String getTaskId() {
        String taskId = "TASK";
        long id = this.generate("gateway:http:server:id");
        if(id<=100000000){
            id=this.generate("gateway:http:server:id",100000000);
        }
        return taskId+id;
    }

    @Resource(name = "redisTemplate2")
    RedisTemplate redisTemplate;

    /**
     * @param key
     * @param value
     * @param expireTime
     * @Title: set
     * @Description: set cache.
     */
    public void set(String key, int value, Date expireTime) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.set(value);
        counter.expireAt(expireTime);
    }

    /**
     * @param key
     * @param value
     * @param timeout
     * @param unit
     * @Title: set
     * @Description: set cache.
     */
    public void set(String key, int value, long timeout, TimeUnit unit) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.set(value);
        counter.expire(timeout, unit);
    }

    /**
     * @param key
     * @return
     * @Title: generate
     * @Description: Atomically increments by one the current value.
     */
    public long generate(String key) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return counter.incrementAndGet();
    }

    /**
     * @param key
     * @return
     * @Title: generate
     * @Description: Atomically increments by one the current value.
     */
    public long generate(String key, Date expireTime) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.expireAt(expireTime);
        return counter.incrementAndGet();
    }

    /**
     * @param key
     * @param increment
     * @return
     * @Title: generate
     * @Description: Atomically adds the given value to the current value.
     */
    public long generate(String key, int increment) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return counter.addAndGet(increment);
    }

    /**
     * @param key
     * @param increment
     * @param expireTime
     * @return
     * @Title: generate
     * @Description: Atomically adds the given value to the current value.
     */
    public long generate(String key, int increment, Date expireTime) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.expireAt(expireTime);
        return counter.addAndGet(increment);
    }

}
