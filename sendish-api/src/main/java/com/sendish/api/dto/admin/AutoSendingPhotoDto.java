package com.sendish.api.dto.admin;

import com.sendish.api.dto.PhotoDto;

public class AutoSendingPhotoDto extends PhotoDto {

	private static final long serialVersionUID = 1L;

	private String cityName;
	private Long cityId;
	private String countryName;
	private Long countryId;
	private Boolean active;

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public Long getCityId() {
		return cityId;
	}

	public void setCityId(Long cityId) {
		this.cityId = cityId;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public Long getCountryId() {
		return countryId;
	}

	public void setCountryId(Long countryId) {
		this.countryId = countryId;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
