package com.sendish.api.controller.api.v1;

import com.sendish.api.dto.CommentDto;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/photo-comments")
@Api(value = "photo-comments", description = "Comments on photo")
public class PhotoCommentsController {

    @RequestMapping(value = "/{photoId}/comments", method = RequestMethod.GET)
    @ApiOperation(value = "Get comments for a specific photo")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    public List<CommentDto> details(@PathVariable Long photoId, @RequestParam(defaultValue = "0") Integer page) {
        return new LinkedList<>();
    }

}
