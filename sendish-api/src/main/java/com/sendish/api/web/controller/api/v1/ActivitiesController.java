package com.sendish.api.web.controller.api.v1;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sendish.api.dto.ActivityItemDto;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.UserActivityServiceImpl;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1.0/activities")
@Api(value = "activities", description = "User activities")
public class ActivitiesController {
	
	@Autowired
	private UserActivityServiceImpl userActivityService;

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get my activity stream")
    @ApiResponses({
    	@ApiResponse(code = 200, message = "OK")
    })
    public List<ActivityItemDto> getActivityList(@RequestParam(defaultValue = "0") Integer page, AuthUser user) {
        return userActivityService.getActivitites(user.getUserId(), page);
    }

}
