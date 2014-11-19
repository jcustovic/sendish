package com.sendish.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class IndexController {

    @RequestMapping(method = RequestMethod.GET)
    public String index() {
        return "index";
    }

    @RequestMapping(value = { "/documentation", "/swagger-ui" }, method = RequestMethod.GET)
    public String documentationRedirect() {
        return "redirect:/swagger-ui/doc";
    }

    @RequestMapping(value = "/swagger-ui/doc", method = RequestMethod.GET)
    public String swaggerUI() {
        return "swagger";
    }

}
