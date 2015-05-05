package com.sendish.repository;

import com.sendish.repository.model.jpa.City;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Transactional(propagation = Propagation.MANDATORY)
public interface CityRepository extends JpaRepository<City, Long> {

    City findByExternalId(Integer externalId);

    @Query("FROM City c WHERE c.deleted = false ORDER BY ST_Distance(c.location.location, ST_SetSRID(ST_MakePoint(?1, ?2), 4326))")
    List<City> findNearest(BigDecimal longitude, BigDecimal latitude, Pageable pageable);

}
