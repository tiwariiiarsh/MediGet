package com.example.MediSearch.service;

import com.example.MediSearch.payload.AuthenticationResult;
import com.example.MediSearch.security.request.LoginRequest;
import com.example.MediSearch.security.request.SignupRequest;
import com.example.MediSearch.security.response.MessageResponse;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthenticationResult login(LoginRequest loginRequest);

    ResponseEntity<MessageResponse> register(SignupRequest signUpRequest);

    Object getCurrentUserDetails(Authentication authentication);

    ResponseCookie getUserLogOut();
}
