package com.sendish.api.distributor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;

@Service
public class RedisBasedDistributorImpl implements PhotoDistributor {

    @Autowired
    private UserPool userPool;

    @Override
    public boolean sendPhoto(Long photoId) {
        // TODO: Implement smart offset
        Collection<String> users = userPool.getNext(10);
        Iterator<String> iterator = users.iterator();
        while (iterator.hasNext()) {
            String userId = iterator.next();
            // STEP 1: Try to lock with timeout
            // STEP 2: Can we send to that user
            // STEP 3: Send photo
            // STEP 4: Remove from list
            // STEP 5: Unlock
        }

        return false;
    }

}
