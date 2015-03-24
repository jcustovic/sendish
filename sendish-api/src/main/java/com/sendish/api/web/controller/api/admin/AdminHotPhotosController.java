package com.sendish.api.web.controller.api.admin;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;

@RestController
@RequestMapping("/api/admin/hot-photos")
@Api(value = "hot-photos", description = "Manage hot photos list")
public class AdminHotPhotosController {

    @Autowired
    private HotPhotoServiceImpl hotPhotoService;

    @Autowired
    private PhotoServiceImpl photoService;

    @RequestMapping(value = "/add/photo/{photoId}", method = RequestMethod.POST)
    @ApiOperation(value = "Add photo to hot list")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Added"),
            @ApiResponse(code = 400, message = "Photo is already on hot list"),
            @ApiResponse(code = 404, message = "Photo not found")
    })
    public ResponseEntity<Void> addPhotoToHotList(@PathVariable Long photoId) {
        Photo photo = photoService.findOne(photoId);
        if (photo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HotPhoto hotPhoto = hotPhotoService.findByPhotoId(photoId);
        if (hotPhoto == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        hotPhotoService.addNewPhoto(photoId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{photoId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Remove photo from hot photo list")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Removed"),
            @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<Void> activate(@PathVariable Long photoId) {
        HotPhoto hotPhoto = hotPhotoService.findByPhotoId(photoId);
        if (hotPhoto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        hotPhotoService.remove(photoId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
