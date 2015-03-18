package com.sendish.api.web.controller.api.admin;

import com.wordnik.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user-inbox")
@Api(value = "inbox", description = "Manipulate with users inbox items")
public class AdminUserInboxController {
}
