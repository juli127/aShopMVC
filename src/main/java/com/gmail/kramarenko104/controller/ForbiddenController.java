package com.gmail.kramarenko104.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/forbidden")
public class ForbiddenController {

    public ForbiddenController() {
    }

    public String doGet() {
        return "forbidden";
    }
}
