package com.sendish.api.service.admin.imp;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sendish.api.dto.LocationBasedFileUpload;
import com.sendish.api.dto.PhotoDto;
import com.sendish.api.dto.admin.AutoSendingPhotoDto;
import com.sendish.api.mapper.PhotoDtoMapper;
import com.sendish.api.service.impl.CityServiceImpl;
import com.sendish.api.service.impl.PhotoStatisticsServiceImpl;
import com.sendish.api.store.FileStore;
import com.sendish.api.util.ImageUtils;
import com.sendish.repository.AutoSendingPhotoRepository;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.AutoSendingPhoto;
import com.sendish.repository.model.jpa.City;
import com.sendish.repository.model.jpa.Location;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.User;

@Service
@Transactional
public class AutoSendingPhotoServiceImpl {
	
	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final String AUTO_PHOTO_USER_OWNER = "auto_photo_sender";
	private static final int MAX_LOCATION_DISPLAY_NAME_LENGTH = 100;
	
	@Autowired
	private AutoSendingPhotoRepository autoSendingPhotoRepository;
	
	@Autowired
	private PhotoDtoMapper photoDtoMapper;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PhotoRepository photoRepository;
	
	@Autowired
	private CityServiceImpl cityService;
	
	@Autowired
	private FileStore fileStore;
	
	@Autowired
    private PhotoStatisticsServiceImpl photoStatisticsService;

	public List<AutoSendingPhotoDto> findAll(Integer page) {
		Page<AutoSendingPhoto> autoPhotos = autoSendingPhotoRepository.findAll(new PageRequest(page, DEFAULT_PAGE_SIZE));
		
		return autoPhotos.getContent().stream().map(a -> mapToAutoSendingPhotoDto(a)).collect(Collectors.toList());
	}

	private AutoSendingPhotoDto mapToAutoSendingPhotoDto(AutoSendingPhoto autoPhoto) {
		AutoSendingPhotoDto autoPhotoDto = new AutoSendingPhotoDto();
		photoDtoMapper.mapToPhotoDto(autoPhoto.getPhoto(), autoPhotoDto, MAX_LOCATION_DISPLAY_NAME_LENGTH);
		autoPhotoDto.setId(autoPhoto.getId());
		autoPhotoDto.setActive(autoPhoto.getActive());
		if (autoPhoto.getCity() != null) {
			autoPhotoDto.setCityName(autoPhoto.getCity().getName());
			autoPhotoDto.setCityId(autoPhoto.getCity().getId());
		}
		if (autoPhoto.getCountry() != null) {
			autoPhotoDto.setCountryName(autoPhoto.getCountry().getName());
			autoPhotoDto.setCountryId(autoPhoto.getCountry().getId());
		}
		
		return autoPhotoDto;
	}

	public List<PhotoDto> findAllAvailablePhotos(Integer page) {
		User photoOwner = userRepository.findByUsernameIgnoreCase(AUTO_PHOTO_USER_OWNER);
		List<Photo> photos = photoRepository.findByUserId(photoOwner.getId(), new PageRequest(page, DEFAULT_PAGE_SIZE, Direction.DESC, "createdDate"));

		return photoDtoMapper.mapToPhotoDto(photos, MAX_LOCATION_DISPLAY_NAME_LENGTH);
	}

	public void processNewPhoto(LocationBasedFileUpload upload) {
		User photoOwner = userRepository.findByUsernameIgnoreCase(AUTO_PHOTO_USER_OWNER);
        Photo photo = mapToPhoto(upload, photoOwner);

        MultipartFile file = upload.getImage();
        Dimension dimension;
        try {
            dimension = ImageUtils.getDimension(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read image dimensions for file " + file.getName());
        }
        photo.setWidth((int) dimension.getWidth());
        photo.setHeight((int) dimension.getHeight());

        String fileStoreId;
        try {
            fileStoreId = fileStore.save(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        photo.setStorageId(fileStoreId);

        Location location = new Location(upload.getLatitude(), upload.getLongitude());
        City city = cityService.findNearest(upload.getLatitude(), upload.getLongitude());
        photo.setOriginLocation(location);
        photo.setCity(city);

        photo = photoRepository.save(photo);
        photoStatisticsService.createNew(photo.getId());
	}
	
	private Photo mapToPhoto(LocationBasedFileUpload upload, User user) {
        Photo photo = new Photo();
        photo.setUser(user);
        photo.setName(upload.getImage().getName());
        photo.setContentType(upload.getImage().getContentType());
        photo.setSize(upload.getImage().getSize());
        photo.setDescription(upload.getDescription());
        photo.setUuid(UUID.randomUUID().toString());

        return photo;
    }

	public Photo findPhotoById(Long photoId) {
		return photoRepository.findOne(photoId);
	}

	public void addNewPhoto(Long photoId) {
		Photo photo = photoRepository.findOne(photoId);
		AutoSendingPhoto autoSendingPhoto = new AutoSendingPhoto();
		autoSendingPhoto.setPhoto(photo);
		autoSendingPhoto.setActive(true);
		
		autoSendingPhotoRepository.save(autoSendingPhoto);
	}

	public AutoSendingPhoto findOne(Long autoSendingPhotoId) {
		return autoSendingPhotoRepository.findOne(autoSendingPhotoId);
	}

	public void deactivate(Long autoSendingPhotoId) {
		AutoSendingPhoto autoSendingPhoto = autoSendingPhotoRepository.findOne(autoSendingPhotoId);
		autoSendingPhoto.setActive(false);
		
		autoSendingPhotoRepository.save(autoSendingPhoto);
	}
	
	public void activate(Long autoSendingPhotoId) {
		AutoSendingPhoto autoSendingPhoto = autoSendingPhotoRepository.findOne(autoSendingPhotoId);
		autoSendingPhoto.setActive(true);
		
		autoSendingPhotoRepository.save(autoSendingPhoto);
	}

}
