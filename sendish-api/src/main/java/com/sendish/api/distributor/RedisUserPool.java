package com.sendish.api.distributor;

import com.sendish.api.redis.KeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RedisUserPool implements UserPool {

    private BoundZSetOperations<String, String> userPool;

    @Autowired
    public RedisUserPool(StringRedisTemplate template) {
        userPool = template.boundZSetOps(KeyUtils.usersPool());
    }

    @Override
    public Long getPoolSize() {
        return userPool.size();
    }

    @Override
    public String getNext() {
        Set<String> results = userPool.range(0, 0);
        if (results.isEmpty()) {
            return null;
        } else {
            return results.iterator().next();
        }
    }

    @Override
    public Collection<String> getNext(int size) {
        return userPool.range(0, size - 1);
    }

    @Override
    public Collection<String> getNext(int start, int end) {
        return userPool.range(start, end);
    }

    @Override
    public UserWithScore getLastWithScore() {
        Set<ZSetOperations.TypedTuple<String>> results = userPool.reverseRangeWithScores(0, 0);
        if (results.isEmpty()) {
            return null;
        } else {
            ZSetOperations.TypedTuple<String> lastOne = results.iterator().next();
            return new UserWithScore(lastOne.getValue(), lastOne.getScore().longValue());
        }
    }
    
    @Override
    public UserWithScore getFirstWithScore() {
        Set<ZSetOperations.TypedTuple<String>> results = userPool.rangeWithScores(0, 0);
        if (results.isEmpty()) {
            return null;
        } else {
            ZSetOperations.TypedTuple<String> lastOne = results.iterator().next();
            return new UserWithScore(lastOne.getValue(), lastOne.getScore().longValue());
        }
    }

    @Override
    public void put(UserWithScore user) {
        userPool.add(user.getUserId(), user.getScore());
    }

    @Override
    public void put(List<UserWithScore> users) {
        userPool.add(getTuple(users));
    }

    @Override
    public void remove(String userId) {
        userPool.remove(userId);
    }

    private Set<ZSetOperations.TypedTuple<String>> getTuple(List<UserWithScore> users) {
        return users.stream()
                .map(tuple -> new DefaultTypedTuple<>(tuple.getUserId(), tuple.getScore().doubleValue()))
                .collect(Collectors.toSet());
    }

}
