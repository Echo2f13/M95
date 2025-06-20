package com.m95.market95;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    // Match all paths that do not start with /api or static files
    @GetMapping(value = { "/", "/**/{path:[^\\.]*}" })
    public String forward() {
        // Forward to React's index.html
        return "forward:/index.html";
    }
}
