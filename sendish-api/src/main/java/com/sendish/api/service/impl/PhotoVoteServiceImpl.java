package com.sendish.api.service.impl;

import com.sendish.api.photo.HotPhotoDecider;
import com.sendish.api.photo.PhotoStopDecider;
import com.sendish.repository.PhotoReceiverRepository;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.repository.PhotoVoteRepository;

@Service
@Transactional
public class PhotoVoteServiceImpl {
	
	@Autowired
	private PhotoVoteRepository photoVoteRepository;

	@Autowired
	private PhotoServiceImpl photoService;

	@Autowired
	private PhotoReceiverRepository photoReceiverRepository;

	@Autowired
	private AsyncPhotoSenderServiceImpl asyncPhotoSenderService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private StatisticsServiceImpl statisticsService;

	@Autowired
	private RankingServiceImpl rankingService;

	@Autowired
	private UserActivityServiceImpl userActivityService;

	@Autowired
	private HotPhotoDecider hotPhotoDecider;

	@Autowired
	private PhotoStopDecider photoStopDecider;

	@Autowired
	private PhotoSenderServiceImpl photoSenderService;
	
	public PhotoVote findOne(Long photoId, Long userId) {
		return photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
	}

	// TODO: Maybe allow changing dislike to like?
	public void likePhoto(Long photoId, Long userId) {
		PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
		if (vote == null) {
			checkAndAddPhotoToReceivedPhotoAndMarkAsOpened(photoId, userId, false);
			processPhotoLike(photoId, userId);
		}
	}

	public void likeReceived(Long photoId, Long userId) {
		PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
		if (vote == null) {
			processPhotoLike(photoId, userId);
			PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
			asyncPhotoSenderService.resendPhotoOnLike(photoId, photoReceiver.getId());
		}
	}

	private void processPhotoLike(Long photoId, Long userId) {
		Photo photo = photoService.findOne(photoId);
		User photoOwner = photo.getUser();

		if (photoOwner.getId().equals(userId)) {
			throw new IllegalStateException("User cannot like his own photo. UserId: " + userId + ", PhotoId: " + photoId);
		}

		User user = userRepository.findOne(userId);
		savePhotoVote(photo, user, true);

		Long likeCount = statisticsService.likePhoto(photoId, photoOwner);
		hotPhotoDecider.decide(photoId, likeCount);
		rankingService.addPointsForLikedPhoto(photoOwner);

		if (!photo.getDeleted()) {
			userActivityService.addPhotoLikedActivity(photo, user);
		}
	}

	// TODO: Maybe allow changing like to dislike?
	public void dislikeReceived(Long photoId, Long userId) {
		PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
		if (vote == null) {
			processPhotoDislike(photoId, userId);
			PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
			photoReceiver.setDeleted(true);
			photoReceiverRepository.save(photoReceiver);

			if (photoStopDecider.checkToStop(photoId)) {
				photoSenderService.stopSending(photoId, "Stopped after dislike check");
			}
		}
	}

	public void dislikePhoto(Long photoId, Long userId) {
		PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
		if (vote == null) {
			checkAndAddPhotoToReceivedPhotoAndMarkAsOpened(photoId, userId, true);
			processPhotoDislike(photoId, userId);
		}
	}

	private void processPhotoDislike(Long photoId, Long userId) {
		Photo photo = photoService.findOne(photoId);
		User photoOwner = photo.getUser();

		if (photoOwner.getId().equals(userId)) {
			throw new IllegalStateException("User cannot dislike his own photo. UserId: " + userId + ", PhotoId: " + photoId);
		}

		User user = userRepository.findOne(userId);
		savePhotoVote(photo, user, false);

		statisticsService.dislikePhoto(photoId, photoOwner);
	}

	public void reportReceived(Long photoId, String reason, String reasonText, Long userId) {
		PhotoVote vote = photoVoteRepository.findOne(new PhotoVoteId(userId, photoId));
		if (vote == null) {
			PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
			User photoOwner = photoReceiver.getPhoto().getUser();

			if (photoOwner.getId().equals(userId)) {
				throw new IllegalStateException("User cannot report his own photo. UserId: " + userId + ", PhotoId: " + photoId);
			}

			vote = new PhotoVote();
			vote.setPhoto(photoReceiver.getPhoto());
			vote.setUser(photoReceiver.getUser());
			vote.setLike(false);
			vote.setReport(true);
			vote.setReportType(reason);
			vote.setReportText(reasonText);
			photoVoteRepository.save(vote);

			photoReceiver.setDeleted(true);
			photoReceiverRepository.save(photoReceiver);

			statisticsService.reportPhoto(photoId, photoOwner);

			if (photoStopDecider.checkToStop(photoId)) {
				photoSenderService.stopSending(photoId, "Stopped after report check");
			}
		}
	}

	private void checkAndAddPhotoToReceivedPhotoAndMarkAsOpened(Long photoId, Long userId, boolean deleted) {
		PhotoReceiver photoReceiver = photoReceiverRepository.findByPhotoIdAndUserId(photoId, userId);
		if (photoReceiver == null) {
			Photo photo = photoService.findOne(photoId);
			User user = userRepository.findOne(userId);
			photoReceiver = new PhotoReceiver();
			photoReceiver.setAutoReceived(false);
			photoReceiver.setDeleted(deleted);
			photoReceiver.setPhoto(photo);
			photoReceiver.setUser(user);

			photoService.saveAndMarkReceivedPhotoAsOpened(photoReceiver);
			photoService.addPhotoToUsersViewedList(userId, photoId);
		} else if (photoReceiver.getOpenedDate() == null) {
			photoService.saveAndMarkReceivedPhotoAsOpened(photoReceiver);
		}
	}

	private PhotoVote savePhotoVote(Photo photo, User user, boolean liked) {
		PhotoVote vote = new PhotoVote();
		vote.setPhoto(photo);
		vote.setUser(user);
		vote.setLike(liked);

		return photoVoteRepository.save(vote);
	}

}
