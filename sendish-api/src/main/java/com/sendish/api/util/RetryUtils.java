package com.sendish.api.util;

import java.util.function.Supplier;

import org.springframework.dao.DataIntegrityViolationException;

public class RetryUtils {
	
	public static <T> T retry(Supplier<T> logic, int retryCount, int sleepMillis) {
    	int retryCounter = 3;
    	while (true) {
    		try {
		        return logic.get();
    		} catch (DataIntegrityViolationException e) {
    			if (--retryCounter == 0) {
    				throw e;
    			} else {
    				try {
						Thread.sleep(10L);
					} catch (InterruptedException e1) {
						// Nothing to do
					}
    			}
    		}
    	}
    }

}
