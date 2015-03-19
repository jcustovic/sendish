package com.sendish.api.web.controller.api.v1;

import com.sendish.api.dto.InboxItemDto;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.UserInboxServiceImpl;
import com.sendish.repository.model.jpa.UserInboxItem;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/inbox")
@Api(value = "inbox", description = "User inbox")
public class UserInboxController {

    @Autowired
    private UserInboxServiceImpl userInboxService;

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get inbox messages")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<InboxItemDto> list(@RequestParam(defaultValue = "0") Integer page, AuthUser authUser) {
        return userInboxService.findAll(authUser.getUserId(), page);
    }

    @RequestMapping(value = "/{itemId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get inbox item details")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InboxItemDto> inboxItemDetails(@PathVariable Long itemId, AuthUser authUser) throws BindException {
        InboxItemDto userInboxItem = userInboxService.getOne(itemId, authUser.getUserId());
        if (userInboxItem == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(userInboxItem, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/inbox-message/{inboxMessageId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get inbox item details by inboxMessageId", notes = "Call this service when you receive notification type NEW_INBOX_ITEM with referenceId as the inboxMessageId")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InboxItemDto> findByInboxMessageId(@PathVariable Long inboxMessageId, AuthUser authUser) throws BindException {
        InboxItemDto userInboxItem = userInboxService.getByInboxMessageIdAndMarkRead(inboxMessageId, authUser.getUserId());
        if (userInboxItem == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(userInboxItem, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{itemId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete inbox item")
    @ApiResponses({
        @ApiResponse(code = 204, message = "Photo deleted"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<Void> delete(@PathVariable Long itemId, AuthUser authUser) {
        UserInboxItem userInboxItem = userInboxService.findOne(itemId, authUser.getUserId());
        if (userInboxItem == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            userInboxService.delete(itemId, authUser.getUserId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = "/{itemId}/mark-read", method = RequestMethod.PUT)
    @ApiOperation(value = "Mark item as read")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<Void> markRead(@PathVariable Long itemId, AuthUser authUser) {
        UserInboxItem userInboxItem = userInboxService.findOne(itemId, authUser.getUserId());
        if (userInboxItem == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            userInboxService.markRead(itemId, authUser.getUserId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = "/{itemId}/mark-unread", method = RequestMethod.PUT)
    @ApiOperation(value = "Mark item as unread")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<Void> markUnread(@PathVariable Long itemId, AuthUser authUser) {
        UserInboxItem userInboxItem = userInboxService.findOne(itemId, authUser.getUserId());
        if (userInboxItem == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            userInboxService.markUnread(itemId, authUser.getUserId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

}
