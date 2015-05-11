package com.sendish.api.web.controller.api.v1;

import com.sendish.api.dto.ReportTypeDto;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/commons")
@Api(value = "commons", description = "Commons stuff, constants, enums etc.")
public class CommonsController {

    private static final List<ReportTypeDto> REPORT_TYPES;

    static {
        REPORT_TYPES = new ArrayList<>();
        REPORT_TYPES.add(new ReportTypeDto("nudity", "Nudity or sexual content", false));
        REPORT_TYPES.add(new ReportTypeDto("hate", "Hate Speech", false));
        REPORT_TYPES.add(new ReportTypeDto("violence", "Violence", false));
        REPORT_TYPES.add(new ReportTypeDto("other", "Other", true));
    }

    @RequestMapping(value = "/report-types", method = RequestMethod.GET)
    @ApiOperation(value = "Get the list of allowed report types")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK")
    })
    public List<ReportTypeDto> getReportTypes() {
        return REPORT_TYPES;
    }

}
