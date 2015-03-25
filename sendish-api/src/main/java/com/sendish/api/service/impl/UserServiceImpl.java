package com.sendish.api.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sendish.api.dto.ChangePasswordDto;
import com.sendish.api.dto.UserProfileDto;
import com.sendish.api.dto.UserRankDto;
import com.sendish.api.dto.UserSettingsDto;
import com.sendish.api.redis.dto.UserStatisticsDto;
import com.sendish.api.util.CityUtils;
import com.sendish.repository.UserDetailsRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.UserStatisticsRepository;
import com.sendish.repository.model.jpa.City;
import com.sendish.repository.model.jpa.Location;
import com.sendish.repository.model.jpa.User;
import com.sendish.repository.model.jpa.UserDetails;
import com.sendish.repository.model.jpa.UserStatistics;

@Service
@Transactional
public class UserServiceImpl {

    public static final int DEFAULT_SEND_LIMIT_PER_DAY = 20;
    public static final int DEFAULT_RECEIVE_LIMIT_PER_DAY = 50;
    public static final int MINUTES_BETWEEN_RECEIVED_PHOTOS = 10;

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
    private StatisticsServiceImpl statisticsService;
    
    @Autowired
    private RankingServiceImpl rankingService;

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
            userProfileDto.setLastPlace(CityUtils.getTrimmedLocationName(userDetails.getCurrentCity()));
            userProfileDto.setLastLat(userDetails.getLocation().getLatitude());
            userProfileDto.setLastLng(userDetails.getLocation().getLongitude());
            userProfileDto.setLastLocationTime(userDetails.getLastInteractionTime().toDate());
        }

        User user = userDetails.getUser();
        userProfileDto.setEmailRegistration(user.getEmailRegistration());
        userProfileDto.setNick(user.getNickname());

        Long userRank = rankingService.getRank(userId);
        if (userRank == null) {
            userProfileDto.setRank("No rank");
        } else {
            userProfileDto.setRank(userRank.toString());
        }
        
        UserStatisticsDto userStatistics = statisticsService.getUserStatistics(userId);
        userProfileDto.setTotalDislikes(userStatistics.getTotalDislikeCount());
        userProfileDto.setTotalLikes(userStatistics.getTotalLikeCount());
        userProfileDto.setCitiesCount(userStatistics.getTotalCityCount());
        userProfileDto.setUnseenPhotoCount(userStatistics.getUnseenPhotoCount());
        userProfileDto.setDailySendLimitLeft(getSentLimitLeft(userDetails, userStatistics));
        userProfileDto.setNewActivities(userStatistics.getHasNewActivities());
        userProfileDto.setUnreadInboxItemCount(userStatistics.getUnreadInboxItemCount());
        userProfileDto.setId(userId);

        return userProfileDto;
    }

    public boolean isSentLimitReached(Long userId) {
        UserDetails userDetails = getUserDetails(userId);

        return userDetails.getSendAllowedTime() != null && userDetails.getSendAllowedTime().isAfterNow();
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

        Long currentCount = statisticsService.incrementUserDailySentPhotoCount(userId, photoDate.toLocalDate());

        if (currentCount >= userDetails.getSendLimitPerDay()) {
            userDetails.setSendAllowedTime(photoDate.withTimeAtStartOfDay().plusDays(1));
        }

        userDetailsRepository.save(userDetails);
    }

    public UserSettingsDto getSettings(Long userId) {
        UserDetails userDetails = getUserDetails(userId);
        UserSettingsDto userSettings = new UserSettingsDto();
        userSettings.setNickname(userDetails.getUser().getNickname());
        userSettings.setReceiveLimitPerDay(userDetails.getReceiveLimitPerDay());
        userSettings.setReceiveNewPhotoNotifications(userDetails.getReceiveNewPhotoNotifications());
        userSettings.setReceiveCommentNotifications(userDetails.getReceiveCommentNotifications());

        return userSettings;
    }

    public void updateSettings(UserSettingsDto userSettings, Long userId) {
        UserDetails userDetails = getUserDetails(userId);
        if (userSettings.getReceiveLimitPerDay() != null) {
            userDetails.setReceiveLimitPerDay(userSettings.getReceiveLimitPerDay());
        }
        userDetails.setReceiveNewPhotoNotifications(userSettings.getReceiveNewPhotoNotifications());
        userDetails.setReceiveCommentNotifications(userSettings.getReceiveCommentNotifications());
        
        if (StringUtils.hasText(userSettings.getNickname())) {
        	userDetails.getUser().setNickname(userSettings.getNickname().trim());
        	userRepository.save(userDetails.getUser());
        }

        userDetailsRepository.save(userDetails);
    }

    public boolean canReceivePhoto(Long userId) {
        UserDetails userDetails = getUserDetails(userId);

        return (userDetails.getLastReceivedTime() == null || userDetails.getLastReceivedTime().isBefore(DateTime.now().minusMinutes(MINUTES_BETWEEN_RECEIVED_PHOTOS)))
        		&& (userDetails.getReceiveAllowedTime() == null || userDetails.getReceiveAllowedTime().isBeforeNow());
    }

	public List<UserRankDto> getTop100() {
		return rankingService.getFromTop(0, 99);
	}

	public User findByUsernameIgnoreCaseOrEmailIgnoreCase(String p_username, String p_email) {
		return userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(p_username, p_email);
	}

	public User findOne(Long userId) {
		return userRepository.findOne(userId);
	}

    public void changePassword(ChangePasswordDto changePassword) {
        User user = findOne(changePassword.getUserId());
        user.setPassword(shaPasswordEncoder.encodePassword(changePassword.getNewPassword(), null));

        String oldPass = shaPasswordEncoder.encodePassword(changePassword.getOldPassword(), null);
        if (!oldPass.equals(user.getPassword())) {
            throw new IllegalArgumentException("Old password doesn't match");
        }

        userRepository.save(user);
    }

}
