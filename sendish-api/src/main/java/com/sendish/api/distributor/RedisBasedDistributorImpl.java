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
            // STEP 1: Try to lock (with timeout - 10s?)
            // STEP 2: Can we send to that user (check all conditions; limits, already received etc.)

            // STEP 3a: No - Unlock!
            // STEP 4a: Break and try next user.

            // STEP 3b: Yes - Send photo
            // STEP 4b: Remove from list, keep lock and return!!!
        }

        return false;
    }

}
