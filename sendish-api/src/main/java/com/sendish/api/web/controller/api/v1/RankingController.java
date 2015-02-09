package com.sendish.api.web.controller.api.v1;

import com.sendish.api.dto.UserRankDto;
import com.sendish.api.service.impl.UserServiceImpl;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/ranking")
@Api(value = "ranking", description = "Ranking - get leaderboard")
public class RankingController {
	
	@Autowired
	private UserServiceImpl userService;

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get TOP 100 users", notes = "Users are ordered by rank ASC")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<UserRankDto> getTopRank() {
        return userService.getTopRank();
    }

}
