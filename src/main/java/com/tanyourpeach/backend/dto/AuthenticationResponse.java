package com.tanyourpeach.backend.dto;

public class AuthenticationResponse {

    private String token;

    public AuthenticationResponse() {
        // Default constructor required for Jackson
    }

    public AuthenticationResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}