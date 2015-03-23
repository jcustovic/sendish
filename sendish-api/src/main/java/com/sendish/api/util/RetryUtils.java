package com.sendish.api.util;

import java.util.function.Supplier;

public class RetryUtils {
	
	public static <T> T retry(Supplier<T> logic, int retryCount, int sleepMillis) {
    	while (true) {
    		try {
		        return logic.get();
    		} catch (Exception e) {
    			if (--retryCount == 0) {
    				throw e;
    			} else {
    				try {
						Thread.sleep(sleepMillis);
					} catch (InterruptedException e1) {
						// Nothing to do
					}
    			}
    		}
    	}
    }

}
