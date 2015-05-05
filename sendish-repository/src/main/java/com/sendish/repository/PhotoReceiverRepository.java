package com.sendish.repository;

import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.PhotoReceiver;

import org.joda.time.DateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(propagation = Propagation.MANDATORY)
public interface PhotoReceiverRepository extends JpaRepository<PhotoReceiver, Long> {

    @Query("SELECT pr.photo FROM PhotoReceiver pr WHERE pr.user.id = ?1 AND pr.photo.uuid = ?2")
    Photo findPhotoByUserIdAndPhotoUUID(Long userId, String photoUUID);

    @Query("SELECT pr FROM PhotoReceiver pr LEFT JOIN FETCH pr.photo LEFT JOIN FETCH pr.city WHERE pr.user.id = ?1 AND pr.deleted = false AND pr.autoReceived = true")
    List<PhotoReceiver> findAutoReceivedByUserId(Long userId, Pageable pageRequest);

    @Query("SELECT pr FROM PhotoReceiver pr WHERE pr.photo.id = ?1 AND pr.user.id = ?2 AND pr.deleted = false")
    PhotoReceiver findNotDeletedByPhotoIdAndUserId(Long photoId, Long userId);

    @Query("SELECT pr FROM PhotoReceiver pr WHERE pr.photo.id = ?1 AND pr.user.id = ?2")
    PhotoReceiver findByPhotoIdAndUserId(Long photoId, Long userId);

    List<PhotoReceiver> findByPhotoIdAndOpenedDateNotNull(Long photoId, Pageable pageRequest);

    @Modifying
    @Query("UPDATE PhotoReceiver pr SET pr.deleted = true WHERE pr.openedDate IS NULL AND pr.createdDate < ?1 AND pr.deleted = false")
	Integer deleteUnopenedOlderThan(DateTime olderThan);
    
    @Modifying
    @Query("UPDATE PhotoReceiver pr SET pr.deleted = true WHERE pr.openedDate IS NULL AND pr.photo.id = ?1 AND pr.deleted = false")
	Integer deleteUnopenedByPhotoId(Long photoId);

}
