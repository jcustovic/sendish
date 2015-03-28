package com.sendish.api.web.controller.api.v1;

import javax.mail.MessagingException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mangofactory.swagger.annotations.ApiIgnore;
import com.sendish.api.dto.UserRegistration;
import com.sendish.api.service.impl.RegistrationServiceImpl;
import com.sendish.api.web.controller.model.ValidationError;
import com.sendish.api.web.controller.validator.UserRegistrationValidator;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1.0/registration")
@Api(value = "registration", description = "Email registration, forgotten password")
public class RegistrationController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationController.class);

    @Autowired
    private UserRegistrationValidator userRegistrationValidator;
    
    @Autowired
    private RegistrationServiceImpl registrationService;

    @InitBinder("userRegistration")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(userRegistrationValidator);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "Register new user", notes = "This method will register a new user and send email confirmation")
    @ApiResponses({ 
    	@ApiResponse(code = 200, message = "Registration is successful and emails was sent", response = Void.class),
    	@ApiResponse(code = 400, message = "Malformed JSON or validation error (model is provided in case of validation error)", response = ValidationError.class)
    })
    public void register(@Valid @RequestBody UserRegistration userRegistration) {
    	registrationService.registerNewUser(userRegistration);
    }

    @ApiIgnore
    @RequestMapping(value = "/verify", method = { RequestMethod.POST, RequestMethod.GET }) // TODO: It should be POST but for simplicity and be able to invoke as a <a href...>
    public ResponseEntity<Void> verifyRegistration(@RequestParam String token, @RequestParam String username) {
        boolean success = false;
        if (StringUtils.hasText(username) && StringUtils.hasText(token)) {
            success = registrationService.verifyRegistrationToken(token, username);
        }

        if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/reset-password/{username}", method = RequestMethod.POST)
    @ApiOperation(value = "Request password reset", notes = "Only if the user signed with email registration!")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Reset password email is sent (NOTE: Only if a user exists in the system).", response = Void.class)
    })
    public ResponseEntity<Void> sendResetPasswordEmail(@PathVariable String username) {
    	try {
			registrationService.sendResetPasswordEmail(username);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (MessagingException e) {
			LOGGER.error("Error resetting password", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
    @RequestMapping(value = "/reset-password/change", method = RequestMethod.POST)
    @ApiOperation(value = "Submit change password", notes = "After user click on reset password link via email.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Password is changed", response = Void.class),
        @ApiResponse(code = 400, message = "Valildation errors.", response = Void.class),
        @ApiResponse(code = 404, message = "Invalid user", response = Void.class)
    })
    public ResponseEntity<Void> resetPassword(@RequestParam String username, @RequestParam String token, @RequestParam String newPassword) {
    	boolean success = registrationService.verifyTokenaAndChangePassword(token, username, newPassword);
    	
    	if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
