package com.sendish.api.photo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sendish.api.redis.dto.PhotoStatisticsDto;
import com.sendish.api.redis.repository.RedisStatisticsRepository;

@Component
public class PhotoStopDecider {
	
	@Autowired
	private RedisStatisticsRepository statisticsRepository;

	public boolean checkToStop(Long photoId) {
		PhotoStatisticsDto photoStatistics = statisticsRepository.getPhotoStatistics(photoId);
		
		double minLikePercent = 0;
		long maxAllowedReport = Long.MAX_VALUE;
		if (photoStatistics.getOpenedCount() >= 100) {
			minLikePercent = 0.8;
		} else if (photoStatistics.getOpenedCount() >= 20) {
			minLikePercent = 0.7;
			maxAllowedReport = 4;
		} else if (photoStatistics.getOpenedCount() >= 10) {	
			minLikePercent = 0.6;
			maxAllowedReport = 3;
		} else if (photoStatistics.getOpenedCount() >= 5) {
			minLikePercent = 0.4;
			maxAllowedReport = 2;
		} else {
			maxAllowedReport = 2;
		}
		
		return checkIfLessThanPercent(photoStatistics.getLikeCount(), photoStatistics.getOpenedCount(), minLikePercent) 
				|| checkReportCountLessThan(photoStatistics.getReportCount(), maxAllowedReport);
	}

	private boolean checkIfLessThanPercent(long likeCount, long openedCount, double minLikePercent) {
		return (likeCount * 1f / openedCount) < minLikePercent;
	}
	
	private boolean checkReportCountLessThan(long reportCount, long maxAllowedReport) {
		return reportCount > maxAllowedReport;
	}

}
