package com.sendish.api.service.impl;

import com.sendish.api.dto.UserProfileDto;
import com.sendish.api.dto.UserRankDto;
import com.sendish.api.dto.UserSettingsDto;
import com.sendish.api.redis.dto.UserStatisticsDto;
import com.sendish.api.redis.repository.RedisStatisticsRepository;
import com.sendish.repository.UserDetailsRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.UserStatisticsRepository;
import com.sendish.repository.model.jpa.*;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl {

    public static final int DEFAULT_SEND_LIMIT_PER_DAY = 20;
    public static final int DEFAULT_RECEIVE_LIMIT_PER_DAY = 50;
    public static final int MINUTES_BETWEEN_RECEIVED_PHOTOS = 15;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private CityServiceImpl cityService;

    @Autowired
    private UserStatisticsRepository userStatisticsRepository;

    @Autowired
    private ShaPasswordEncoder shaPasswordEncoder;

    @Autowired
    private RedisStatisticsRepository statisticsRepository;

    public UserDetails getUserDetails(Long userId) {
        return userDetailsRepository.findOne(userId);
    }

    public UserDetails saveUserDetails(UserDetails userDetails) {
        return userDetailsRepository.save(userDetails);
    }

    public User createUser(String username, String email, String password, String nickname, boolean emailRegistration) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(shaPasswordEncoder.encodePassword(password, null));
        user.setNickname(nickname);
        user.setEmailRegistration(emailRegistration);

        String verificationCode = UUID.randomUUID().toString();
        user.setVerificationCode(verificationCode);

        user = userRepository.save(user);

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(user.getId());
        userDetails.setSendLimitPerDay(DEFAULT_SEND_LIMIT_PER_DAY);
        userDetails.setReceiveLimitPerDay(DEFAULT_RECEIVE_LIMIT_PER_DAY);
        userDetailsRepository.save(userDetails);

        UserStatistics userStatistics = new UserStatistics();
        userStatistics.setUserId(user.getId());
        userStatisticsRepository.save(userStatistics);

        return user;
    }

    public UserProfileDto getUserProfile(Long userId) {
        UserDetails userDetails = getUserDetails(userId);

        UserProfileDto userProfileDto = new UserProfileDto();
        if (userDetails.getLastLocationTime() != null) {
            userProfileDto.setLastPlace(getUserPlaceName(userDetails.getCurrentCity()));
            userProfileDto.setLastLat(userDetails.getLocation().getLatitude());
            userProfileDto.setLastLng(userDetails.getLocation().getLongitude());
            userProfileDto.setLastLocationTime(userDetails.getLastInteractionTime().toDate());
        }

        User user = userDetails.getUser();
        userProfileDto.setEmailRegistration(user.getEmailRegistration());
        userProfileDto.setNick(user.getNickname());

        UserStatisticsDto userStatistics = statisticsRepository.getUserStatistics(userId);
        if (userStatistics.getRank() == 0L) {
            userProfileDto.setRank("No rank");
        } else {
            userProfileDto.setRank(String.valueOf(userStatistics.getRank()));
        }
        userProfileDto.setTotalDislikes(userStatistics.getTotalDislikeCount());
        userProfileDto.setTotalLikes(userStatistics.getTotalLikeCount());
        userProfileDto.setCitiesCount(userStatistics.getTotalCityCount());
        userProfileDto.setUnseenPhotoCount(userStatistics.getUnseenPhotoCount());
        userProfileDto.setDailySendLimitLeft(getSentLimitLeft(userDetails, userStatistics));
        userProfileDto.setId(userId);

        return userProfileDto;
    }

	private String getUserPlaceName(City city) {
		return city.getName() + ", " + city.getCountry().getName();
	}

    public Long getSentLimitLeft(Long userId) {
        UserDetails userDetails = getUserDetails(userId);
        UserStatisticsDto userStatistics = statisticsRepository.getUserStatistics(userId);

        return getSentLimitLeft(userDetails, userStatistics);
    }

    private Long getSentLimitLeft(UserDetails userDetails, UserStatisticsDto userStatistics) {
        return userDetails.getSendLimitPerDay() - userStatistics.getDailySendCount();
    }

    public Location getLastLocation(Long userId) {
        UserDetails userDetails = getUserDetails(userId);

        return userDetails.getLocation();
    }

    public void updateLocation(Long userId, BigDecimal longitude, BigDecimal latitude) {
        UserDetails userDetails = getUserDetails(userId);
        Location currentLocation = userDetails.getLocation();
        if (currentLocation == null || longitude.compareTo(currentLocation.getLongitude()) != 0
                || latitude.compareTo(currentLocation.getLatitude()) != 0) {
            userDetails.setLastLocationTime(DateTime.now());
            userDetails.setLocation(new Location(latitude, longitude));
            userDetails.setCurrentCity(cityService.findNearest(latitude, longitude));
            userDetails.setLastInteractionTime(DateTime.now());

            userDetailsRepository.save(userDetails);
        }
    }

    public void updateStatisticsForNewSentPhoto(Long userId, DateTime photoDate, Location location, City city) {
        UserDetails userDetails = getUserDetails(userId);
        userDetails.setLastSentTime(photoDate);
        userDetails.setLastLocationTime(photoDate);
        userDetails.setLocation(location);
        userDetails.setCurrentCity(city);
        userDetails.setLastInteractionTime(photoDate);

        Long currentCount = statisticsRepository.increaseDailySentPhotoCount(userId, photoDate.toLocalDate());

        if (currentCount >= userDetails.getSendLimitPerDay()) {
            userDetails.setSendAllowedTime(photoDate.withTimeAtStartOfDay().plusDays(1));
        }

        userDetailsRepository.save(userDetails);
    }

    public UserSettingsDto getSettings(Long userId) {
        UserDetails userDetails = getUserDetails(userId);
        UserSettingsDto userSettings = new UserSettingsDto();
        userSettings.setReceiveLimitPerDay(userDetails.getReceiveLimitPerDay());
        userSettings.setReceiveNotifications(userDetails.getReceiveNotifications());

        return userSettings;
    }

    public void updateSettings(UserSettingsDto userSettings, Long userId) {
        UserDetails userDetails = getUserDetails(userId);
        userDetails.setReceiveLimitPerDay(userSettings.getReceiveLimitPerDay());
        userDetails.setReceiveNotifications(userSettings.getReceiveNotifications());

        userDetailsRepository.save(userDetails);
    }

    public boolean canReceivePhoto(Long userId) {
        UserDetails userDetails = getUserDetails(userId);

        return userDetails.getLastReceivedTime().isAfter(DateTime.now().minusMinutes(MINUTES_BETWEEN_RECEIVED_PHOTOS))
                && userDetails.getReceiveAllowedTime().isAfterNow();
    }

	public List<UserRankDto> getTopRank() {
		// TODO: Implement real ranking from redis
		Page<User> users = userRepository.findAll(new PageRequest(0, 100, Sort.Direction.DESC, "createdDate"));
		List<UserRankDto> topUsers = new ArrayList<UserRankDto>();
		int i = 0;
		for (User user : users) {
			String username;
			if (user.getDetails().getCurrentCity() == null) {
				username = "Noname";
			} else {
				username = getUserPlaceName(user.getDetails().getCurrentCity());
			}
			topUsers.add(new UserRankDto(username, String.valueOf(++i), user.getId()));
		}
		
		return topUsers;
	}

}
