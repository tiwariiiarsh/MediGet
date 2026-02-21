package com.example.MediSearch.controller;


import com.example.MediSearch.payload.AuthenticationResult;
import com.example.MediSearch.security.request.LoginRequest;
import com.example.MediSearch.security.request.SignupRequest;
import com.example.MediSearch.security.response.MessageResponse;
import com.example.MediSearch.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        AuthenticationResult result = authService.login(loginRequest);



//        incase of token formate
//        return ResponseEntity.ok(response);

//        incase of cookie formate
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                        result.getJwtCookie().toString())
                .body(result.getResponse());
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return  authService.register(signUpRequest);
    }


    @GetMapping("/username")
    public String currentUserName(Authentication authentication){
        if (authentication!=null){
            return authentication.getName();
        }else {
            return "";
        }
    }


    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(Authentication authentication){
        return ResponseEntity.ok().body(authService.getCurrentUserDetails(authentication));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signoutUser(){
        ResponseCookie cookie = authService.getUserLogOut();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                        cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }



}




//User → /signin with credentials.
//Spring Security → username/password validate karega.
//Agar correct hai → JWT token generate karega.
//Response me JWT token + user info + roles jayega.
//Next requests me client Authorization: Bearer <token> bhejega.

