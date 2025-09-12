package com.knusdp.SmartLedger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController

@RequestMapping("/")

class TestController {



    @GetMapping("/hello")

    public String helloWorld() {

        return "Hello Worlda aaa";

    }

}