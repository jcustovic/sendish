package com.sendish.api.web.controller.api.v1;

import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.UserServiceImpl;
import com.sendish.api.web.controller.model.ValidationError;
import com.sendish.api.dto.ChangePasswordDto;
import com.sendish.api.dto.UserProfileDto;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import scala.None;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1.0/user-profile")
@Api(value = "user-profile", description = "Get user profile information")
public class UserProfileController {

    @Autowired
    private UserServiceImpl userService;

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get the current user profile")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public UserProfileDto getProfile(AuthUser authUser) {
        return userService.getUserProfile(authUser.getUserId());
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
    @ApiOperation(value = "Change user password", notes = "Only if the user signed with email registration!")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Password change successful", response = None.class),
        @ApiResponse(code = 400, message = "Malformed JSON or validation error (model is provided in case of validation error)", response = ValidationError.class)
    })
    public void register(@Valid @RequestBody ChangePasswordDto changePasswordDto, AuthUser authUser) {
        // TODO: Implement me
    }

}