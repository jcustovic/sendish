package com.sendish.api.web.device;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class DeviceUtils {
	
	public static final String DEVICE_ATTRIBUTE_NAME = "_sendish_Device";
	
	public static Device getDevice() {
		return (Device) RequestContextHolder.getRequestAttributes().getAttribute(DEVICE_ATTRIBUTE_NAME, RequestAttributes.SCOPE_REQUEST);
	}

}
