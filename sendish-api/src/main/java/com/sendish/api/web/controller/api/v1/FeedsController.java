package com.sendish.api.web.controller.api.v1;

import com.sendish.api.dto.FeedItemDto;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/feeds")
@Api(value = "feeds", description = "Feed")
public class FeedsController {

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get my stream")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public List<FeedItemDto> myStreamList(@RequestParam(defaultValue = "0") Integer page) {
        // TODO: Implement me
        return new ArrayList<>();
    }

}