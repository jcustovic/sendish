package com.sendish.repository;

import com.sendish.repository.model.jpa.Photo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(propagation = Propagation.MANDATORY)
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    Photo findByUserIdAndUuid(Long userId, String photoUUID);

    @Query("SELECT p FROM Photo p WHERE p.user.id = ?1 AND p.deleted = false AND p.senderDeleted = false")
    List<Photo> findByUserId(Long userId, Pageable page);

    Photo findByIdAndUserId(Long photoId, Long userId);

    Photo findByUuid(String photoUuid);

    @Query("SELECT p FROM Photo p WHERE p.city.country.id = ?1 AND p.deleted = false AND p.senderDeleted = false")
    List<Photo> findByCityCountryId(Long countryId, Pageable page);

}
