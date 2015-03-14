package com.sendish.api.service.impl;

import java.util.List;

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

	public List<String> findUserIdsWithConnection(Connection<?> connection) {
		return usersConnectionRepository.findUserIdsWithConnection(connection);
	}

	public void updateConnection(String userId, Connection<?> connection) {
		// We rely on hibernate to not issue update if nothing changed!
		usersConnectionRepository.createConnectionRepository(userId).updateConnection(connection);
	}

}
