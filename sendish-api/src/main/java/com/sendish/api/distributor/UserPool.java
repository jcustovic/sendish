package com.sendish.api.distributor;

import java.util.List;

public interface UserPool {

    Long getPoolSize();

    Long getNext();

    List<Long> getNext(int n);

    UserWithScore getNextWithScore();

    List<UserWithScore> getNextWithScore(int n);

    void put(UserWithScore user);

    void put(List<UserWithScore> users);

}
