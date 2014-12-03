package com.sendish.api.web.controller.api.v1;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import scala.None;

import com.mangofactory.swagger.annotations.ApiIgnore;
import com.sendish.api.web.controller.model.ValidationError;
import com.sendish.api.web.controller.validator.UserRegistrationValidator;
import com.sendish.api.dto.UserRegistration;
import com.sendish.api.service.impl.RegistrationServiceImpl;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1.0/registration")
@Api(value = "registration", description = "Email registration, forgotten password")
public class RegistrationController {

    @Autowired
    private UserRegistrationValidator userRegistrationValidator;
    
    @Autowired
    private RegistrationServiceImpl registrationService;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(userRegistrationValidator);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "Register new user", notes = "This method will register a new user and send email confirmation")
    @ApiResponses({ 
    	@ApiResponse(code = 200, message = "Registration is successful and emails was sent", response = None.class),
    	@ApiResponse(code = 400, message = "Malformed JSON or validation error (model is provided in case of validation error)", response = ValidationError.class)
    })
    public void register(@Valid @RequestBody UserRegistration userRegistration) {
    	registrationService.registerNewUser(userRegistration);
    }

    @ApiIgnore
    @RequestMapping(value = "/verify", method = { RequestMethod.POST, RequestMethod.GET })
    public ResponseEntity<?> verifyRegistration(@RequestParam String token, @RequestParam String username) {
        boolean success = registrationService.verifyToken(token, username);

        if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/reset-password/{username}", method = RequestMethod.POST)
    @ApiOperation(value = "Request password reset", notes = "Only if the user signed with email registration!")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Reset password email is sent (NOTE: Only if a user exists in the system).", response = None.class)
    })
    public void resetPassword(@Valid @RequestBody UserRegistration userRegistration) {
        // TODO:
    }

}
