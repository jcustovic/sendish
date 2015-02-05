package com.sendish.repository;

import com.sendish.repository.model.jpa.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ImageRepository extends JpaRepository<Image, Long> {

    Image findByUuid(String uuid);

}
