package com.sendish.api.service.impl;

import com.sendish.repository.UserDetailsRepository;
import com.sendish.repository.model.jpa.City;
import com.sendish.repository.model.jpa.Location;
import com.sendish.repository.model.jpa.UserDetails;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserServiceImpl {

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private CityServiceImpl cityService;

    public UserDetails getOrCreateByUserId(Long userId) {
        UserDetails userDetails = userDetailsRepository.findOne(userId);

        if (userDetails == null) {
            userDetails = new UserDetails();
            userDetails.setUserId(userId);
            userDetails.setLastInteractionTime(DateTime.now());
            userDetails = userDetailsRepository.save(userDetails);
        }

        return userDetails;
    }

    public Location getLastLocation(Long userId) {
        UserDetails userDetails = getOrCreateByUserId(userId);

        return userDetails.getLocation();
    }

    public void updateLocation(Long userId, BigDecimal longitude, BigDecimal latitude) {
        UserDetails userDetails = getOrCreateByUserId(userId);
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
        UserDetails userDetails = getOrCreateByUserId(userId);
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
