package com.sendish.api.distributor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;

@Service
public class PhotoDistributorImpl implements PhotoDistributor {

    @Autowired
    private UserPool userPool;

    @Override
    public boolean sendPhoto(Long photoId) {
        Collection<String> users = userPool.getNext(10);
        Iterator<String> iterator = users.iterator();
        while (iterator.hasNext()) {
            String userId = iterator.next();

        }

        return false;
    }

}
