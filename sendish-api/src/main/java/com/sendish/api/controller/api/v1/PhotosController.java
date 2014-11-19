package com.sendish.api.controller.api.v1;

import com.sendish.api.dto.PhotoDetailsDto;
import com.sendish.api.dto.PhotoDto;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/v1.0/photos")
@Api(value = "photos", description = "Photos API - get details, send photos and delete photos")
public class PhotosController {

    @RequestMapping(value = "/received", method = RequestMethod.GET)
    @ApiOperation(value = "Get list of received photos", notes = "This method will return the list of received photos")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public List<PhotoDto> getReceivedPhotos(@RequestParam(defaultValue = "0") Integer page) {
        return getDummyPhotos();
    }

    @RequestMapping(value = "/sent", method = RequestMethod.GET)
    @ApiOperation(value = "Get list of sent photos", notes = "This method will return the list of sent photos")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public List<PhotoDto> getSentPhotos(@RequestParam(defaultValue = "0") Integer page) {
        return getDummyPhotos();
    }

    @RequestMapping(value = "/sendish-upload", method = RequestMethod.POST)
    public void upload (@RequestParam("file") MultipartFile multipartFile) {

    }

    @RequestMapping(value = "/{photoId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get photo details")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public PhotoDetailsDto details(@PathVariable Long photoId) {
        return new PhotoDetailsDto();
    }

    @RequestMapping(value = "/{photoId}/like", method = RequestMethod.PUT)
    @ApiOperation(value = "Like a given photo")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public void like(@PathVariable Long photoId) {

    }

    @RequestMapping(value = "/{photoId}/dislike", method = RequestMethod.PUT)
    @ApiOperation(value = "Dislike a given photo")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public void dislike(@PathVariable Long photoId) {

    }

    @RequestMapping(value = "/{photoId}/report", method = RequestMethod.PUT)
    @ApiOperation(value = "Report a given photo", notes = "Reason must be provided and reasonText is optional")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public void report(@PathVariable Long photoId, @RequestParam String reason, @RequestParam(required = false) String reasonText) {

    }

    public List<PhotoDto> getDummyPhotos() {
        List<PhotoDto> dummyPhotos = new LinkedList<>();
        PrettyTime prettyTime = new PrettyTime();
        PhotoDto photo1 = new PhotoDto();
        photo1.setDescription("Cool photo");
        photo1.setCityCount(10);
        photo1.setCommentCount(4);
        photo1.setLikeCount(7);
        photo1.setImgLocation("test");
        photo1.setCity("Zagreb");
        photo1.setCountry("Croatia");
        photo1.setTimeAgo(prettyTime.format(DateTime.now().minusMinutes(30).toDate()));

        PhotoDto photo2 = new PhotoDto();
        photo2.setDescription("Cool photo no 2");
        photo2.setCityCount(20);
        photo2.setCommentCount(7);
        photo2.setLikeCount(1);
        photo2.setImgLocation("test23");
        photo2.setCity("München");
        photo2.setCountry("Germany");
        photo2.setTimeAgo(prettyTime.format(DateTime.now().minusHours(10).toDate()));

        dummyPhotos.add(photo1);
        dummyPhotos.add(photo2);

        return dummyPhotos;
    }

}
