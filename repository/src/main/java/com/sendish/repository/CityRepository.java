package com.sendish.repository;

import com.sendish.repository.model.jpa.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface CityRepository extends JpaRepository<City, Long> {

    City findByExternalId(Integer externalId);

}
