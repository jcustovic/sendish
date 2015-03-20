package com.sendish.api.web.controller.validator;

import com.sendish.api.dto.ChangePasswordDto;
import com.sendish.repository.UserRepository;
import com.sendish.repository.model.jpa.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ChangePasswordValidator implements Validator {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShaPasswordEncoder shaPasswordEncoder;

    @Override
    public boolean supports(Class<?> clazz) {
        return ChangePasswordDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ChangePasswordDto changePassword = (ChangePasswordDto) target;

        if (StringUtils.hasText(changePassword.getNewPassword()) && StringUtils.hasText(changePassword.getConfirmPassword())) {
            if (!changePassword.getNewPassword().equals(changePassword.getConfirmPassword())) {
                errors.rejectValue("confirmPassword", null, "Passwords don't match");
            }
        }

        if (!errors.hasErrors()) {
            User user = userRepository.findOne(changePassword.getUserId());
            String oldPass = shaPasswordEncoder.encodePassword(changePassword.getOldPassword(), null);
            if (!oldPass.equals(user.getPassword())) {
                errors.rejectValue("oldPassword", null, "Password incorrect");
            }
        }
    }

}
