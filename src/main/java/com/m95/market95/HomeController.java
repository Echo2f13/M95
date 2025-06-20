package com.m95.market95;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";  // This returns the name of the Thymeleaf template "index.html" in templates/
    }
}
