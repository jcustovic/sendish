package com.sendish.repository;

import com.sendish.repository.model.jpa.UserSocialConnection;
import com.sendish.repository.model.jpa.UserSocialConnectionId;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import javax.persistence.QueryHint;

@Transactional(readOnly = true)
public interface UserSocialConnectionRepository extends JpaRepository<UserSocialConnection, UserSocialConnectionId>, JpaSpecificationExecutor<UserSocialConnection>, UserSocialConnectionRepositoryCustom {

	@QueryHints({
			@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"),
			@QueryHint(name = org.hibernate.annotations.QueryHints.CACHE_REGION, value = "com.sendish.repository.UserSocialConnectionRepository.findUserId") 
	})
    @Query("SELECT usc.userId FROM UserSocialConnection usc WHERE usc.providerId = ?1 AND usc.providerUserId = ?2")
    List<Long> findUserId(String p_providerId, String p_providerUserId);

    @Query("SELECT usc.userId FROM UserSocialConnection usc WHERE usc.providerId = :providerId AND usc.providerUserId IN :providerUserIds")
    Set<Long> findUserIdsConnectedTo(@Param("providerId") String p_providerId, @Param("providerUserIds") Set<String> p_providerUserIds);

    List<UserSocialConnection> findByUserId(Long p_userId, Sort p_sort);

    @Query("SELECT usc FROM UserSocialConnection usc WHERE usc.userId IN :userIds")
    List<UserSocialConnection> findByUserIds(@Param("userIds") Set<Long> p_userIds);

    List<UserSocialConnection> findByUserIdAndProviderId(Long p_userId, String p_providerId, Sort p_sort);

    UserSocialConnection findByUserIdAndProviderIdAndProviderUserId(Long p_userId, String p_providerId, String p_providerUserId);

    List<UserSocialConnection> findByUserIdAndProviderIdAndRank(Long p_userId, String p_providerId, Integer p_rank);

    @Query("SELECT COALESCE(MAX(usc.rank) + 1, 1) FROM UserSocialConnection usc WHERE usc.userId = ?1 and usc.providerId = ?2")
    Integer getRankByUserIdAndProviderId(Long p_userId, String p_providerId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSocialConnection usc WHERE usc.userId = ?1 AND usc.providerId = ?2")
    void deleteByUserIdAndProviderId(Long p_userId, String p_providerId);

}
