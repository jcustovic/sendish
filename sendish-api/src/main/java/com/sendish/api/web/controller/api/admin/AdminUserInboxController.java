package com.sendish.api.web.controller.api.admin;

import com.sendish.api.dto.admin.InboxMessageDto;
import com.sendish.api.service.impl.InboxMessageServiceImpl;
import com.sendish.api.service.impl.UserInboxServiceImpl;
import com.sendish.api.service.impl.UserServiceImpl;
import com.sendish.repository.model.jpa.User;
import com.sendish.repository.model.jpa.UserInboxItem;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user-inbox")
@Api(value = "user-inbox", description = "Manipulate with users inbox items")
public class AdminUserInboxController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private InboxMessageServiceImpl inboxMessageService;

    @Autowired
    private UserInboxServiceImpl userInboxService;

    @RequestMapping(value = "/{userId}/send/{inboxMessageId}", method = RequestMethod.POST)
    @ApiOperation(value = "Send inbox message to specific user")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "User already received specified inbox item"),
        @ApiResponse(code = 404, message = "User or inbox message not found")
    })
    public ResponseEntity<Void> sendInboxItemToUser(@PathVariable Long userId, @PathVariable Long inboxMessageId) {
        User user = userService.findOne(userId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        InboxMessageDto inboxMessage = inboxMessageService.findOne(inboxMessageId);
        if (inboxMessage == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        UserInboxItem userInboxItem = userInboxService.findByInboxMessageId(inboxMessageId, userId);
        if (userInboxItem != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        userInboxService.sendInboxMessage(inboxMessageId, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
