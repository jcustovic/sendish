package com.sendish.api.service.impl;

import com.sendish.api.dto.UserProfileDto;
import com.sendish.repository.UserDetailsRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.UserStatisticsRepository;
import com.sendish.repository.model.jpa.*;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl {

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

    public UserDetails getUserDetails(Long userId) {
        return userDetailsRepository.findOne(userId);
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
        userDetails.setUser(user);
        userDetails.setLastInteractionTime(DateTime.now());
        userDetailsRepository.save(userDetails);

        UserStatistics userStatistics = new UserStatistics();
        userStatistics.setUser(user);
        userStatisticsRepository.save(userStatistics);

        return user;
    }

    public UserProfileDto getUserProfile(Long userId) {
        UserDetails userDetails = getUserDetails(userId);
        User user = userDetails.getUser();
        UserStatistics userStatistics = userStatisticsRepository.findByUserId(userId);

        UserProfileDto userProfileDto = new UserProfileDto();
        if (userDetails.getLastLocationTime() != null) {
            userProfileDto.setLastPlace(userDetails.getCurrentCity().getName() + ", " + userDetails.getCurrentCity().getCountry().getName());
            userProfileDto.setLastLat(userDetails.getLocation().getLatitude());
            userProfileDto.setLastLng(userDetails.getLocation().getLongitude());
            userProfileDto.setLastLocationTime(userDetails.getLastInteractionTime().toDate());
        }
        userProfileDto.setEmailRegistration(user.getEmailRegistration());
        userProfileDto.setNick(user.getNickname());
        userProfileDto.setRank(userStatistics.getRank());
        userProfileDto.setTotalDislikes(userStatistics.getDislikes());
        userProfileDto.setTotalLikes(userStatistics.getLikes());
        userProfileDto.setCitiesCount(userStatistics.getCities());
        userProfileDto.setId(userId);

        return userProfileDto;
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

    public void updateLocation(Long userId, Location location, City city) {
        UserDetails userDetails = getUserDetails(userId);
        Location currentLocation = userDetails.getLocation();
        if (currentLocation == null || location.getLongitude().compareTo(currentLocation.getLongitude()) != 0
                || location.getLatitude().compareTo(currentLocation.getLatitude()) != 0) {
            userDetails.setLastLocationTime(DateTime.now());
            userDetails.setLocation(location);
            userDetails.setCurrentCity(city);
            userDetails.setLastInteractionTime(DateTime.now());

            userDetailsRepository.save(userDetails);
        }
    }

}
