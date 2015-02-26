package com.sendish.api.web.controller.api.v1;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sendish.api.dto.PhotoDto;
import com.sendish.api.service.impl.HotPhotoServiceImpl;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1.0/hot-photos")
@Api(value = "hot-photos", description = "Hot Photos API - get list")
public class HotPhotosController {
	
	@Autowired
	private HotPhotoServiceImpl hotPhotoService;
	
	@RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get hot photo list")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<PhotoDto> list(@RequestParam(defaultValue = "0") Integer page) {
        return hotPhotoService.findAllActive(page);
    }

}
