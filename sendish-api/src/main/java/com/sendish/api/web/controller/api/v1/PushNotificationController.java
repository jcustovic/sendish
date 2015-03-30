package com.sendish.api.web.controller.api.v1;

import com.sendish.api.notification.AsyncNotificationProvider;
import com.sendish.api.security.userdetails.AuthUser;
import com.sendish.api.service.impl.NotificationServiceImpl;
import com.sendish.repository.model.jpa.ApnsPushToken;
import com.sendish.repository.model.jpa.GcmPushToken;
import com.wordnik.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/push-notifications")
@Api(value = "push-notifications", description = "Manage push notifications and token manipulation")
public class PushNotificationController {

    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private AsyncNotificationProvider notificationProvider;

    @RequestMapping(value = "/apns/token/{token}", method = RequestMethod.PUT)
    @ApiOperation(value = "Register APNS token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public final void registerApns(@ApiParam("APNS Token") @PathVariable final String token, @RequestParam(defaultValue = "false") Boolean devToken, AuthUser user) {
        notificationService.registerApns(token, user.getUserId(), devToken);
    }

    @RequestMapping(value = "/gcm/token/{token}", method = RequestMethod.PUT)
    @ApiOperation(value = "Register GCM token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerGcm(@ApiParam("GCM Token") @PathVariable final String token, AuthUser user) {
        notificationService.registerGcm(token, user.getUserId());
    }

    @RequestMapping(value = "/apns/token/{token}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Unregister APNS token")
    @ApiResponses({
        @ApiResponse(code = 204, message = "Token unregistered"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<Void> unregisterApns(@ApiParam("APNS Token") @PathVariable final String token, AuthUser user) {
        ApnsPushToken apnsToken = notificationService.findApnsToken(token, user.getUserId());
        if (apnsToken == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            notificationService.unregisterApns(token, user.getUserId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = "/gcm/token/{token}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Unregister GCM token")
    @ApiResponses({
        @ApiResponse(code = 204, message = "Token unregistered"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<Void> unregisterGcm(@ApiParam("GCM Token") @PathVariable final String token, AuthUser user) {
        GcmPushToken gcmToken = notificationService.findGcmToken(token, user.getUserId());
        if (gcmToken == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            notificationService.unregisterGcm(token, user.getUserId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = "/test-push", method = RequestMethod.POST)
    @ApiOperation(value = "Send test notification to all users devices")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendTestNotification(@RequestParam final String message, @RequestBody Map<String, Object> customFields, AuthUser user) {
        if (customFields == null || customFields.isEmpty()) {
            notificationProvider.sendPlainTextNotification(message, user.getUserId());
        } else {
            notificationProvider.sendPlainTextNotification(message, customFields, user.getUserId());
        }
    }

}
