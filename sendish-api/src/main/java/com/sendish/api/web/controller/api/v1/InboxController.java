package com.sendish.api.web.controller.api.v1;

import com.sendish.api.dto.InboxItemDto;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(value = "/{itemId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete inbox item")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Long itemId) {
        // TODO: Implement me
    }

    @RequestMapping(value = "/{itemId}/mark-read", method = RequestMethod.PUT)
    @ApiOperation(value = "Mark item as read")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public ResponseEntity<Void> markRead(@PathVariable Long itemId) {
        // TODO: Implement me
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
