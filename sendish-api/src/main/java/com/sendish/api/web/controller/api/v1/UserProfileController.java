package com.sendish.api.web.controller.api.v1;

import com.sendish.api.dto.LocationDto;
import com.sendish.api.dto.UserSettingsDto;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.UserServiceImpl;
import com.sendish.api.web.controller.model.ValidationError;
import com.sendish.api.dto.ChangePasswordDto;
import com.sendish.api.dto.UserProfileDto;
import com.sendish.api.web.controller.validator.ChangePasswordValidator;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.wordnik.swagger.annotations.Api;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1.0/user-profile")
@Api(value = "user-profile", description = "Get user profile information")
public class UserProfileController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ChangePasswordValidator changePasswordValidator;

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
        @ApiResponse(code = 200, message = "NOT USED! 204 will be returned"),
        @ApiResponse(code = 204, message = "Password change successful", response = Void.class),
        @ApiResponse(code = 400, message = "Malformed JSON or validation error (model is provided in case of validation error)", response = ValidationError.class)
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordDto changePassword, BindingResult bindingResult, AuthUser authUser) throws BindException {
        changePassword.setUserId(authUser.getUserId());
        changePasswordValidator.validate(changePassword, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        } else {
            userService.changePassword(changePassword);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = "/update-location", method = RequestMethod.POST)
    @ApiOperation(value = "Update user location")
    @ApiResponses({
        @ApiResponse(code = 204, message = "User location was updated", response = Void.class),
        @ApiResponse(code = 400, message = "Malformed JSON or validation error (model is provided in case of validation error)", response = ValidationError.class)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLocation(@Valid @RequestBody LocationDto newLocation, AuthUser authUser) {
        userService.updateLocation(authUser.getUserId(), newLocation.getLongitude(), newLocation.getLatitude());
    }

    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    @ApiOperation(value = "Get user settings")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public UserSettingsDto getSettings(AuthUser authUser) {
        return userService.getSettings(authUser.getUserId());
    }

    @RequestMapping(value = "/settings", method = RequestMethod.PUT)
    @ApiOperation(value = "Update user settings")
    @ApiResponses({
        @ApiResponse(code = 204, message = "User settings were updated", response = Void.class),
        @ApiResponse(code = 400, message = "Malformed JSON or validation error (model is provided in case of validation error)", response = ValidationError.class)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSettings(@Valid @RequestBody UserSettingsDto userSettings, AuthUser authUser) {
        userService.updateSettings(userSettings, authUser.getUserId());
    }

}
