package com.sendish.api.controller.api.v1;

import com.sendish.api.controller.model.ValidationError;
import com.sendish.api.controller.validator.UserRegistrationValidator;
import com.sendish.api.dto.UserRegistration;
import com.sendish.api.service.impl.RegistrationServiceImpl;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import scala.None;

import javax.validation.Valid;

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
    @ApiOperation(value = "Register new user", notes = "This method will register a new user and send email configration")
    @ApiResponses({ 
    	@ApiResponse(code = 200, message = "Registration is successful and emails was sent", response = None.class),
    	@ApiResponse(code = 400, message = "Malformed JSON or validation error (model is provided in case of validation error)", response = ValidationError.class)
    })
    public void register(@Valid @RequestBody UserRegistration userRegistration) {
    	registrationService.registerNewUser(userRegistration);
    }

}
