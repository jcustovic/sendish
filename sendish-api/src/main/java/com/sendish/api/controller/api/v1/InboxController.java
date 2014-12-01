package com.sendish.api.controller.api.v1;

import com.sendish.api.dto.InboxItemDto;
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
@RequestMapping("/api/v1.0/inbox")
@Api(value = "inbox", description = "User inbox")
public class InboxController {

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get inbox messages")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public List<InboxItemDto> list(@RequestParam(defaultValue = "0") Integer page) {
        return new ArrayList<>();
    }

}
