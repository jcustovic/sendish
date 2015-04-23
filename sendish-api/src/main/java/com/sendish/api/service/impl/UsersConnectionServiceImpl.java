package com.sendish.api.service.impl;

import java.util.List;

import com.sendish.api.util.EntitySynchronizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsersConnectionServiceImpl {

	@Autowired
	private UsersConnectionRepository usersConnectionRepository;

	@Autowired
	private EntitySynchronizer entitySynchronizer;

	public List<String> findUserIdsWithConnection(Connection<?> connection) {
		// This is introduced to not create user twice if multiple threads call this method.
		// Underlying method should has its own independent transaction!
		entitySynchronizer.lock(connection.getKey());
		try {
			return usersConnectionRepository.findUserIdsWithConnection(connection);
		} finally {
			entitySynchronizer.unlock();
		}
	}

	public void updateConnection(String userId, Connection<?> connection) {
		// We rely on hibernate to not issue update if nothing changed!
		usersConnectionRepository.createConnectionRepository(userId).updateConnection(connection);
	}

}
