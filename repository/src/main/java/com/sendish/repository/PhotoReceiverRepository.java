package com.sendish.repository;

import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoReceiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface PhotoReceiverRepository extends JpaRepository<PhotoReceiver, Long> {

    @Query("SELECT pr.photo FROM PhotoReceiver pr WHERE pr.user.id = ?1 AND pr.photo.uuid = ?2")
    Photo findPhotoByUserIdAndPhotoUUID(Long userId, String photoUUID);

}
