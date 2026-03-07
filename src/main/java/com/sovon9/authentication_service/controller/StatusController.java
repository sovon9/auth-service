package com.sovon9.authentication_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/status")
    public String getStatus()
    {
        return "<h1> Auth service is working ... </h1>";
    }
}
