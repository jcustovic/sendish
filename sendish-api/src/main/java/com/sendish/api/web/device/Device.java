package com.sendish.api.web.device;

public class Device {

	private DeviceType type;
	private String version;

	public Device() {
		super();
		type = DeviceType.UNKNOWN;
	}

	public Device(DeviceType type, String version) {
		super();
		this.type = type;
		this.version = version;
	}

	// the strings are equal or one string is a substring of the other
	// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
	public Integer isVersionGreatherThan(String minVersion) {
		String[] vals1 = minVersion.split("\\.");
		String[] vals2 = version.split("\\.");
		int i = 0;
		// set index to first non-equal ordinal or length of shortest version
		// string
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}
		// compare first non-equal ordinal number
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		} else {
			return Integer.signum(vals1.length - vals2.length);
		}
	}
	
	public static void main(String[] args) {
		Device device = new Device();
		device.setVersion("1.0");
		System.out.println(device.isVersionGreatherThan("1.0"));
		System.out.println(device.isVersionGreatherThan("1.1"));
		System.out.println(device.isVersionGreatherThan("0.99"));
		System.out.println(device.isVersionGreatherThan("1.0.1"));
		System.out.println(device.isVersionGreatherThan("1.0.0"));
	}
	
	// Getters & setters

	public DeviceType getType() {
		return type;
	}

	public void setType(DeviceType type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
