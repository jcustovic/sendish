package com.sendish.api.distributor;

import com.sendish.api.redis.KeyUtils;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RedisBasedDistributorImpl implements PhotoDistributor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisBasedDistributorImpl.class);

    public static final int USER_LOCK_EXPIRE_IN_SECONDS = 60;
    public static final int MAX_USER_FOR_SLOT_CALCULATION = 2000;

    @Autowired
    private UserPool userPool;

    @Autowired
    private PhotoServiceImpl photoService;

    private StringRedisTemplate redisTemplate;
    
    private final Map<Integer, int[]> slotMap;

    @Autowired
    public RedisBasedDistributorImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        slotMap = Collections.synchronizedMap(new HashMap<Integer, int[]>());
    }
    
    
    @Override
	public List<PhotoReceiver> resendPhoto(Long photoId) {
		return sendPhoto(photoId, true, 1);
	}

	@Override
	public List<PhotoReceiver> sendNewPhoto(Long photoId) {
		return sendPhoto(photoId, false, 5);
	}

    private List<PhotoReceiver> sendPhoto(Long photoId, boolean checkForAlreadyReceived, int receiverCount) {
		Long poolSize = userPool.getPoolSize(); // TODO: Maybe local caching for few min?
    	UserBlock userBlock = getUserBlock(receiverCount, poolSize);
        try {
			LOGGER.debug("Got user block {} for photo {} (Pool size: {})", userBlock, photoId, userPool.getPoolSize());
        	Photo photo = photoService.findOne(photoId);
        	String photoOwnerIdString = photo.getUser().getId().toString();
	    	Collection<String> users = userBlock.getUsers();
	    	List<PhotoReceiver> receivers = new ArrayList<>();
	    	if (users.isEmpty()) {
	    		LOGGER.debug("Users block is empty! No users for for photo {}", photoId);
	    		return receivers;
	    	}
	    	
	        Iterator<String> iterator = users.iterator();
	        while (iterator.hasNext() && receiverCount-- > 0) {
	        	String userIdString = iterator.next();
	        	if (!photoOwnerIdString.equals(userIdString)) {
	        		PhotoReceiver receiver = trySendingPhotoToUser(photoId, checkForAlreadyReceived, userIdString);
	        		if (receiver != null) {
	        			receivers.add(receiver);
	        		}
	        	}
	        }
	        
	        return receivers;
        } finally {
        	releaseUserBlock(userBlock);
        }
    }

    /**
     * STEP 1: Try to lock (with timeout 60s)
     * STEP 2: Has user received a photo if check is required?
     *
     * STEP 3a: Yes: Unlock and try next user!
     * STEP 3b: No: Send photo, remove user from the list and return true
     *
     * NOTE: If we don't find any user return null!
     */
	private PhotoReceiver trySendingPhotoToUser(Long photoId, boolean checkForAlreadyReceived, String userIdString) {
		Long userId = Long.valueOf(userIdString);
		
		if (lockUser(userId)) {
		    if (checkForAlreadyReceived && photoService.hasAlreadyReceivedPhoto(photoId, userId)) {
		        unlockUser(userId);
		    } else {
		        PhotoReceiver photoReceiver = photoService.sendPhotoToUser(photoId, userId);
		        userPool.remove(userIdString);

		        return photoReceiver;
		    }
		} else {
			LOGGER.debug("Failed to lock user {} for photo {}", userIdString, photoId);
		}
		
		return null;
	}

	private UserBlock getUserBlock(int requiredSize, Long poolSize) {
    	int blockSize = requiredSize * 2;
    	if (blockSize > MAX_USER_FOR_SLOT_CALCULATION) {
    		throw new IllegalArgumentException("Required size must not be greater than " + MAX_USER_FOR_SLOT_CALCULATION / 2);
    	}
    	LOGGER.trace("Trying to get slot for block size {}", blockSize);
    	int slot = getBestSlotForBlockSize(blockSize, poolSize);
    	LOGGER.trace("Slot {} found for block size {}", slot, blockSize);
    	
    	int start = slot * blockSize;
    	int end = start + blockSize - 1;
    
    	LOGGER.trace("Fetching users... start: {}, end: {}", start, end);
    	Collection<String> users = userPool.getNext(start, end);
    	LOGGER.trace("Got {} users", users.size());
    	
    	return new UserBlock(blockSize, slot, users);
	}
	
	private void releaseUserBlock(UserBlock userBlock) {
		int[] slots = slotMap.get(userBlock.getBlockSize());
		synchronized (slots) {
			int newOccupancy = --slots[userBlock.getSlot()];
			LOGGER.trace("Released slot {} (new occupancy: {}) for block size {}", userBlock.getSlot(), newOccupancy, userBlock.getBlockSize());
		}
	}

	private int getBestSlotForBlockSize(Integer blockSize, Long maxUserSize) {
		int[] possibleSlots = slotMap.putIfAbsent(blockSize, new int[MAX_USER_FOR_SLOT_CALCULATION / blockSize]);
		if (possibleSlots == null) {
			possibleSlots = slotMap.get(blockSize);
		}
		int maxPossibleSlotIndex = (int) (maxUserSize / blockSize);
		if (maxPossibleSlotIndex == 0) {
			maxPossibleSlotIndex++;
		}
		int maxSlotIndex = Math.min(maxPossibleSlotIndex, possibleSlots.length);
		synchronized (possibleSlots) {
			int minOccupancy = Integer.MAX_VALUE;
			int minIndex = 0;
			for (int index = 0; index < maxSlotIndex - 1; index++) {
				int currentSlotOccupancy = possibleSlots[index];
				if (currentSlotOccupancy == 0) {
					possibleSlots[index]++;
					return index;
				} else if (currentSlotOccupancy < minOccupancy) {
					minOccupancy = currentSlotOccupancy;
					minIndex = index;
				}
			}
			
			return minIndex;
		}
	}

	private void unlockUser(Long userId) {
        redisTemplate.delete(KeyUtils.usersPoolLock(userId));
    }

    private boolean lockUser(Long userId) {
        // TODO: Replace with "SET key value [EX seconds] [PX milliseconds] [NX|XX]" (Starting with Redis 2.6.12)
        BoundValueOperations<String, String> userLock = userLock(userId);
        Boolean result = userLock.setIfAbsent("L");
        if (result) {
            userLock.expire(USER_LOCK_EXPIRE_IN_SECONDS, TimeUnit.SECONDS);
        }

        return result;
    }

    public BoundValueOperations<String, String> userLock(long userId) {
        return redisTemplate.boundValueOps(KeyUtils.usersPoolLock(userId));
    }
    
    private class UserBlock {
    	
    	private int blockSize;
    	private int slot;
    	private Collection<String> users;

		public UserBlock(int blockSize, int slot, Collection<String> users) {
			this.blockSize = blockSize;
			this.slot = slot;
			this.users = users;
		}
		
		public int getBlockSize() {
			return blockSize;
		}

		public int getSlot() {
			return slot;
		}

		public Collection<String> getUsers() {
			return users;
		}
		
		@Override
		public String toString() {
			return String.format("BlockSize: %d, slot: %d, Users size: %d", blockSize, slot, users.size());
		}
		
    }

}
