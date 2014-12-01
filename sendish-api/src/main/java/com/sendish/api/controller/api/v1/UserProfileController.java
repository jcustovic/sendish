package com.sendish.api.controller.api.v1;

import com.sendish.api.dto.UserProfileDto;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;

@RestController
@RequestMapping("/api/v1.0/user-profile")
@Api(value = "user-profile", description = "Get user profile information")
public class UserProfileController {

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get the current user profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public UserProfileDto getProfile() {
        return new UserProfileDto();
    }

}
