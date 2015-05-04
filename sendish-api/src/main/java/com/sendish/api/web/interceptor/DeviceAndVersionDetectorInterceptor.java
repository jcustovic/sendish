package com.sendish.api.web.interceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.sendish.api.web.device.Device;
import com.sendish.api.web.device.DeviceType;
import com.sendish.api.web.device.DeviceUtils;

public class DeviceAndVersionDetectorInterceptor extends HandlerInterceptorAdapter {

	private static final Pattern IOS_VERSION_PATTERN = Pattern.compile("sendish/([^\\s]+)");

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String userAgent = request.getHeader("User-Agent");
		Device device = new Device();
		if (userAgent != null) {
			userAgent = userAgent.toLowerCase();
			if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
				device.setType(DeviceType.IOS);
				Matcher matcher = IOS_VERSION_PATTERN.matcher(userAgent);
				if (matcher.find()) {
				    String version = matcher.group(1);
				    device.setVersion(version);
				}
			}
		}
		
		request.setAttribute(DeviceUtils.DEVICE_ATTRIBUTE_NAME, device);
		
		return true;
	}

}
