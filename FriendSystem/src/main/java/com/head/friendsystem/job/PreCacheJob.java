package com.head.friendsystem.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.head.friendsystem.mapper.UserMapper;
import com.head.friendsystem.model.domain.User;
import com.head.friendsystem.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *  缓存预热任务
 */

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L);

    // 每天执行，预热推荐用户
    @Scheduled(cron = "0 58 23 * * *")
    public void doCacheRecommendUser()  {
        RLock lock = redissonClient.getLock("head:precachejob:docache:lock");
        try {
            if(lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) { //等待时间，过期时间

                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("head:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    // 查询数据库后并写入缓存
                    try {
                        // 一定要设置过期时间！！！！！！
                        valueOperations.set(redisKey, userPage, 100000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }

            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error",e);
        } finally {
            // 只能释放自己的锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

}
