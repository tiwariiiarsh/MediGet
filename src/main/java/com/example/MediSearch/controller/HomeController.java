package com.example.MediSearch.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "MediSearch Backend Running Successfully 🚀";
    }
}
