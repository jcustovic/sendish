package com.sendish.api.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.sendish.api.dto.CommonPhotoDetailsDto;
import com.sendish.api.dto.PhotoDetailsDto;
import com.sendish.api.service.impl.PhotoCommentServiceImpl;
import com.sendish.repository.PhotoVoteRepository;
import com.sendish.repository.model.jpa.PhotoVote;
import com.sendish.repository.model.jpa.PhotoVoteId;
import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sendish.api.dto.PhotoDto;
import com.sendish.api.redis.dto.PhotoStatisticsDto;
import com.sendish.api.service.impl.StatisticsServiceImpl;
import com.sendish.api.util.CityUtils;
import com.sendish.repository.model.jpa.HotPhoto;
import com.sendish.repository.model.jpa.Photo;

@Component
public class PhotoDtoMapper {

    private static final int MAX_COMMENTS_IN_PHOTO_DETAILS = 3;

	private static PrettyTime prettyTime = new PrettyTime();
	
	@Autowired
	private StatisticsServiceImpl statisticsService;

    @Autowired
    private PhotoCommentServiceImpl photoCommentService;

    @Autowired
    private PhotoVoteRepository photoVoteRepository;

    public List<PhotoDto> mapHotToPhotoDto(List<HotPhoto> photos, int maxLocationNameLength) {
        return photos.stream().map(photo -> mapToPhotoDto(photo.getPhoto(), maxLocationNameLength)).collect(Collectors.toList());
    }
	
	public List<PhotoDto> mapToPhotoDto(List<Photo> photos, int maxLocationNameLength) {
        return photos.stream().map(photo -> mapToPhotoDto(photo, maxLocationNameLength)).collect(Collectors.toList());
    }
	
	public PhotoDto mapToPhotoDto(Photo photo, int locationMaxLength) {
		PhotoDto photoDto = new PhotoDto();
		
		return mapToPhotoDto(photo, photoDto, locationMaxLength);
	}
	
	public PhotoDto mapToPhotoDto(Photo photo, PhotoDto photoDto, int locationMaxLength) {
        photoDto.setId(photo.getId());
        photoDto.setOwnerUserId(photo.getUser().getId());
        photoDto.setOriginLocation(CityUtils.getLocationName(photo.getCity(), locationMaxLength));
        photoDto.setDescription(photo.getDescription());
        photoDto.setTimeAgo(getPrettyTime(photo.getCreatedDate()));
        photoDto.setUuid(photo.getUuid());

        PhotoStatisticsDto stats = statisticsService.getPhotoStatistics(photo.getId());
        photoDto.setCityCount(stats.getCityCount());
        photoDto.setCommentCount(stats.getCommentCount());
        photoDto.setLikeCount(stats.getLikeCount());
        photoDto.setDislikeCount(stats.getDislikeCount());

        return photoDto;
    }

    public CommonPhotoDetailsDto mapToCommonPhotoDetailsDto(Photo photo, Long userId, int locationMaxLength) {
        CommonPhotoDetailsDto photoDetails = new CommonPhotoDetailsDto();
        mapToCommonPhotoDetailsDto(photo, photoDetails, userId, locationMaxLength);

        return photoDetails;
    }

    public void mapToCommonPhotoDetailsDto(Photo photo, CommonPhotoDetailsDto photoDetailsDto, Long userId, int locationMaxLength) {
        mapToPhotoDto(photo, photoDetailsDto, locationMaxLength);
        photoDetailsDto.setComments(photoCommentService.findFirstByPhotoId(photo.getId(), userId, MAX_COMMENTS_IN_PHOTO_DETAILS));
    }

    public PhotoDetailsDto mapToPhotoDetailsDto(Photo photo, Long userId, int locationMaxLength) {
        PhotoDetailsDto photoDetails = new PhotoDetailsDto();

        mapToPhotoDto(photo, photoDetails, locationMaxLength);

        photoDetails.setComments(photoCommentService.findFirstByPhotoId(photo.getId(), userId, MAX_COMMENTS_IN_PHOTO_DETAILS));
        PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photo.getId()));
        if (vote == null) {
            photoDetails.setForceRating(true);
        } else {
            photoDetails.setLike(vote.getLike());
        }

        return photoDetails;
    }
	
	private String getPrettyTime(DateTime dateTime) {
        return prettyTime.format(dateTime.toDate());
    }

}
