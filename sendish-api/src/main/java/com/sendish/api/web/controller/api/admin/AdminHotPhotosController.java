package com.sendish.api.web.controller.api.admin;

import com.sendish.api.dto.PhotoDto;
import com.sendish.api.service.impl.HotPhotoServiceImpl;
import com.sendish.api.service.impl.PhotoServiceImpl;
import com.sendish.repository.model.jpa.HotPhoto;
import com.sendish.repository.model.jpa.Photo;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wordnik.swagger.annotations.Api;

import java.util.List;

@RestController
@RequestMapping("/api/admin/hot-photos")
@Api(value = "hot-photos", description = "Manage hot photos list")
public class AdminHotPhotosController {

    @Autowired
    private HotPhotoServiceImpl hotPhotoService;

    @Autowired
    private PhotoServiceImpl photoService;

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get hot photo list (active and inactive!)")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<PhotoDto> list(@RequestParam(defaultValue = "0") Integer page) {
        return hotPhotoService.findAll(page);
    }

    @RequestMapping(value = "/add/photo/{photoId}", method = RequestMethod.POST)
    @ApiOperation(value = "Add photo to hot list")
    @ApiResponses({
        @ApiResponse(code = 200, message = "NOT USED! 204 will be returned"),
        @ApiResponse(code = 204, message = "Added"),
        @ApiResponse(code = 400, message = "Photo is already on hot list"),
        @ApiResponse(code = 404, message = "Photo not found")
    })
    public ResponseEntity<Void> addPhotoToHotList(@PathVariable Long photoId) {
        Photo photo = photoService.findOne(photoId);
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HotPhoto hotPhoto = hotPhotoService.findByPhotoId(photoId);
        if (hotPhoto != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        hotPhotoService.newHotPhoto(photoId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/{photoId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Remove photo from hot photo list")
    @ApiResponses({
        @ApiResponse(code = 200, message = "NOT USED! 204 will be returned"),
        @ApiResponse(code = 204, message = "Removed"),
        @ApiResponse(code = 400, message = "Already removed"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<Void> activate(@PathVariable Long photoId) {
        HotPhoto hotPhoto = hotPhotoService.findByPhotoId(photoId);
        if (hotPhoto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (hotPhoto.getRemovedTime() != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        hotPhotoService.remove(photoId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
