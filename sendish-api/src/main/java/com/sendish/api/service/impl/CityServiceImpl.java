package com.sendish.api.service.impl;

import com.sendish.repository.CityRepository;
import com.sendish.repository.model.jpa.City;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CityServiceImpl {

    @Autowired
    private CityRepository cityRepository;

    public City findNearest(BigDecimal p_latitude, BigDecimal p_longitude) {
        List<City> cities = cityRepository.findNearest(p_longitude, p_latitude, new PageRequest(0, 1));

        return cities.get(0);
    }

    public City findOne(Long cityId) {
        return cityRepository.findOne(cityId);
    }

}
