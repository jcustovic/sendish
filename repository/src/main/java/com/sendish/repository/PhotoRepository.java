package com.sendish.repository;

import com.sendish.repository.model.jpa.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    Photo findByUuid(String uuid);

}
