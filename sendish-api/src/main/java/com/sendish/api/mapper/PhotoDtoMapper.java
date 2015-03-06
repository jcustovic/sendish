package com.sendish.api.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sendish.api.dto.PhotoDto;
import com.sendish.api.redis.dto.PhotoStatisticsDto;
import com.sendish.api.redis.repository.RedisStatisticsRepository;
import com.sendish.api.util.CityUtils;
import com.sendish.repository.model.jpa.HotPhoto;
import com.sendish.repository.model.jpa.Photo;

@Component
public class PhotoDtoMapper {
	
	private static PrettyTime prettyTime = new PrettyTime();
	
	@Autowired
    private RedisStatisticsRepository statisticsRepository;
	
	public List<PhotoDto> mapHotToPhotoDto(List<HotPhoto> photos) {
        return photos.stream().map(photo -> mapToPhotoDto(photo.getPhoto())).collect(Collectors.toList());
    }
	
	public List<PhotoDto> mapToPhotoDto(List<Photo> photos) {
        return photos.stream().map(photo -> mapToPhotoDto(photo)).collect(Collectors.toList());
    }
	
	public PhotoDto mapToPhotoDto(Photo photo) {
		PhotoDto photoDto = new PhotoDto();
		
		return mapToPhotoDto(photo, photoDto);
	}
	
	public PhotoDto mapToPhotoDto(Photo photo, PhotoDto photoDto) {
        photoDto.setId(photo.getId());
        photoDto.setOriginLocation(CityUtils.getLocationName(photo.getCity()));
        photoDto.setDescription(photo.getDescription());
        photoDto.setTimeAgo(getPrettyTime(photo.getCreatedDate()));
        photoDto.setUuid(photo.getUuid());

        PhotoStatisticsDto stats = statisticsRepository.getPhotoStatistics(photo.getId());
        photoDto.setCityCount(stats.getCityCount());
        photoDto.setCommentCount(stats.getCommentCount());
        photoDto.setLikeCount(stats.getLikeCount());
        photoDto.setDislikeCount(stats.getDislikeCount());

        return photoDto;
    }
	
	private String getPrettyTime(DateTime dateTime) {
        return prettyTime.format(dateTime.toDate());
    }

}