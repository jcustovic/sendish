package com.sendish.api.distributor;

import java.util.Collection;
import java.util.List;

public interface UserPool {

    Long getPoolSize();

    String getNext();

    Collection<String> getNext(int size);

    /**
     * start and end are inclusive ranges, so for example  "0 1" will return both the first and the second element of the pool.
     * 
     * @param start
     * @param end
     * @return
     */
    Collection<String> getNext(int start, int end);

    UserWithScore getLastWithScore();
    
    UserWithScore getFirstWithScore();

    void put(UserWithScore user);

    void put(List<UserWithScore> users);

    void remove(String userId);

}
