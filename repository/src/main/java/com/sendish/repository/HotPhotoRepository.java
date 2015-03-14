package com.sendish.repository;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.model.jpa.HotPhoto;

@Transactional(propagation = Propagation.MANDATORY)
public interface HotPhotoRepository extends JpaRepository<HotPhoto, Long> {

	@QueryHints({
        @QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"),
        @QueryHint(name = org.hibernate.annotations.QueryHints.CACHE_REGION, value = "com.sendish.repository.HotPhotoRepository.findAllActive")
    })
	@Query("SELECT hp FROM HotPhoto hp WHERE hp.removedTime IS NULL")
	List<HotPhoto> findAllActive(Pageable page);

	HotPhoto findByPhotoUuid(String photoUUID);

}
