package com.sendish.api.web.device;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class DeviceUtils {
	
	public static final String DEVICE_ATTRIBUTE_NAME = "_sendish_Device";
	
	public static Device getDevice() {
		return (Device) RequestContextHolder.getRequestAttributes().getAttribute(DEVICE_ATTRIBUTE_NAME, RequestAttributes.SCOPE_REQUEST);
	}

	public static boolean isIOSWithVersionGreatherThan(String version) {
		Device device = getDevice();

		return DeviceType.IOS.equals(device.getType())
				&& version != null
				&& device.isVersionGreatherThan(version) == 1;
	}

	public static boolean isIOS() {
		return DeviceType.IOS.equals(getDevice());
	}

	public static boolean isAndroidWithVersionGreatherThan(String version) {
		Device device = getDevice();

		return DeviceType.ANDROID.equals(device.getType())
				&& version != null
				&& device.isVersionGreatherThan(version) == 1;
	}

	public static boolean isAndroid() {
		return DeviceType.ANDROID.equals(getDevice());
	}

}
