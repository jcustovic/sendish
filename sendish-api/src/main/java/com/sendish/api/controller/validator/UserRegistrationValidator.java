package com.sendish.api.controller.validator;

import com.sendish.api.dto.UserRegistration;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserRegistrationValidator implements Validator {

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
    }

}
