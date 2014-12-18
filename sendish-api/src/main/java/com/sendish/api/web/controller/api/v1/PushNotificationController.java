package com.sendish.api.web.controller.api.v1;

import com.sendish.api.notification.DelegateNotificationProvider;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.NotificationServiceImpl;
import com.wordnik.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/push-notifications")
@Api(value = "push-notifications", description = "Manage push notifications and token manipulation")
public class PushNotificationController {

    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private DelegateNotificationProvider notificationProvider;

    @RequestMapping(value = "/apns/token/{p_token}", method = RequestMethod.PUT)
    @ApiOperation(value = "Register APNS token")
    public final void registerApns(@ApiParam("APNS Token") @PathVariable final String p_token, @RequestParam(defaultValue = "false") Boolean p_devToken, AuthUser user) {
        notificationService.registerApns(p_token, user.getUserId(), p_devToken);
    }

    @RequestMapping(value = "/gcm/token/{p_token}", method = RequestMethod.PUT)
    @ApiOperation(value = "Register GCM token")
    public final void registerGcm(@ApiParam("GCM Token") @PathVariable final String p_token, AuthUser user) {
        notificationService.registerGcm(p_token, user.getUserId());
    }

    @RequestMapping(value = "/apns/token/{p_token}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Unregister APNS token")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Token removed")
    })
    public final void unregisterApns(@ApiParam("APNS Token") @PathVariable final String p_token, AuthUser user) {
        // TODO: 404 if token not found for user
        notificationService.unregisterApns(p_token, user.getUserId());
    }

    @RequestMapping(value = "/gcm/token/{p_token}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Unregister GCM token")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Token removed")
    })
    public final void unregisterGcm(@ApiParam("GCM Token") @PathVariable final String p_token, AuthUser user) {
        // TODO: 404 if token not found for user
        notificationService.unregisterGcm(p_token, user.getUserId());
    }

    @RequestMapping(value = "/test-push", method = RequestMethod.POST)
    @ApiOperation(value = "Send test notification to user devices")
    public final void sendTestNotification(@RequestParam final String message, AuthUser user) {
        notificationProvider.sendPlainTextNotification(message, user.getUserId());
    }

}
