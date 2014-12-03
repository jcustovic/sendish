package com.sendish.api.web.controller.validator;

import com.sendish.api.dto.UserRegistration;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserRegistrationValidator implements Validator {
	
	@Autowired
	private UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserRegistration.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
    	UserRegistration userReg = (UserRegistration) target;
    	if (StringUtils.hasText(userReg.getPassword()) && StringUtils.hasText(userReg.getRepeatPassword())) {
    		if (!userReg.getPassword().equals(userReg.getRepeatPassword())) {
    			errors.rejectValue("repeatPassword", null, "Passwords don't match");
    		}
    	}
    	
    	User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(userReg.getEmail(), userReg.getEmail());
    	if (user != null && Boolean.TRUE.equals(user.getEmailConfirmed())) {
    		errors.rejectValue("email", null, "Email already exists");
    	}
    }

}
