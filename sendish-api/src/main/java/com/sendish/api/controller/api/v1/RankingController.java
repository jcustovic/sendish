package com.sendish.api.controller.api.v1;

import com.sendish.api.dto.UserRankDto;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/ranking")
@Api(value = "ranking", description = "Ranking - get leaderboard")
public class RankingController {

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get TOP 100 users", notes = "Users are ordered by rank ASC")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<UserRankDto> getTopRank() {
        return new ArrayList<>();
    }

}
