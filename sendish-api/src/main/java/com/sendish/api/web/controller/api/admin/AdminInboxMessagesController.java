package com.sendish.api.web.controller.api.admin;

import com.sendish.api.dto.admin.CreateInboxMessage;
import com.sendish.api.dto.admin.InboxMessageDto;
import com.sendish.api.service.impl.InboxMessageServiceImpl;
import com.sendish.api.web.controller.model.ValidationError;
import com.sendish.api.web.controller.validator.CreateInboxMessageValidator;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/inbox-messages")
@Api(value = "inbox-messages", description = "Manage inbox messages")
public class AdminInboxMessagesController {

    @Autowired
    private InboxMessageServiceImpl inboxMessageService;

    @Autowired
    private CreateInboxMessageValidator createInboxMessageValidator;

    @InitBinder("createInboxMessage")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(createInboxMessageValidator);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get inbox messages")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<InboxMessageDto> list(@RequestParam(defaultValue = "0") Integer page) {
        return inboxMessageService.findAll(page);
    }

    @RequestMapping(value = "/{inboxMessageId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get inbox messages details")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<InboxMessageDto> inboxMessageDetails(@PathVariable Long inboxMessageId) throws BindException {
        InboxMessageDto inboxMessageDto = inboxMessageService.findOne(inboxMessageId);
        if (inboxMessageDto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(inboxMessageDto, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ApiOperation(value = "Upload new photo", notes = "If all si OK and you get code 201 check Location header to point you to the newly created photo", response = Void.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "NOT USED! 201 will be returned"),
            @ApiResponse(code = 201, message = "Image upload is successful and the resource is created"),
            @ApiResponse(code = 400, message = "Malformed JSON or validation error (model is provided in case of validation error)", response = ValidationError.class),
            @ApiResponse(code = 429, message = "Upload limit exceeded")
    })
    public ResponseEntity<Void> createNew(@Valid @ModelAttribute CreateInboxMessage createInboxMessage,
                                       MultipartFile image) { // FIXME: MultipartFile image is also specified here because of swagger!
        InboxMessageDto inboxMessageDto = inboxMessageService.createNew(createInboxMessage);

        final URI location = ServletUriComponentsBuilder
                .fromCurrentServletMapping().path("/api/admin/inbox-messages{id}").build()
                .expand(inboxMessageDto.getId()).toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

}
