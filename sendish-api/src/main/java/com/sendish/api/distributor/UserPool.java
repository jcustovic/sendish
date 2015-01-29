package com.sendish.api.distributor;

import java.util.Collection;
import java.util.List;

public interface UserPool {

    Long getPoolSize();

    String getNext();

    Collection<String> getNext(int n);

    Collection<String> getNextWithOffset(int offset, int n);

    UserWithScore getNextWithScore();

    Collection<UserWithScore> getNextWithScore(int n);

    List<UserWithScore> getNextWithOffsetWithScore(int offset, int n);

    void put(UserWithScore user);

    void put(List<UserWithScore> users);

}
