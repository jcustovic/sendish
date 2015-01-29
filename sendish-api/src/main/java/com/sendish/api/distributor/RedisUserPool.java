package com.sendish.api.distributor;

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
        userPool = template.boundZSetOps("pool.users");
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
    public Collection<String> getNext(int n) {
        return userPool.range(0, n);
    }

    @Override
    public Collection<String> getNextWithOffset(int offset, int n) {
        return userPool.range(offset, n);
    }

    @Override
    public UserWithScore getNextWithScore() {
        return null;
    }

    @Override
    public List<UserWithScore> getNextWithScore(int n) {
        return null;
    }

    @Override
    public List<UserWithScore> getNextWithOffsetWithScore(int offset, int n) {
        return null;
    }

    @Override
    public void put(UserWithScore user) {
        userPool.add(user.getUserId(), user.getScore());
    }

    @Override
    public void put(List<UserWithScore> users) {
        userPool.add(getTuple(users));
    }

    private Set<ZSetOperations.TypedTuple<String>> getTuple(List<UserWithScore> users) {
        return users.stream()
                .map(tuple -> new DefaultTypedTuple<>(tuple.getUserId(), tuple.getScore().doubleValue()))
                .collect(Collectors.toSet());
    }

}