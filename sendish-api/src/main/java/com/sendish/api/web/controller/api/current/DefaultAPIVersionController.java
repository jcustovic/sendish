package com.sendish.api.web.controller.api.current;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

@Controller
@RequestMapping("/api/d/**")
public class DefaultAPIVersionController {

    @RequestMapping
    public String forward(HttpServletRequest request) {
    	String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        return "forward:/api/v1.0/" + path.substring(7);
    }

}
