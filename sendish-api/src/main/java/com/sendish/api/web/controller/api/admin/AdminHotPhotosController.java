package com.sendish.api.web.controller.api.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;

@RestController
@RequestMapping("/api/admin/hot-photos")
@Api(value = "hot-photos", description = "Manage hot photos list")
public class AdminHotPhotosController {

}
