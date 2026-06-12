package com.skybooker.auth.security;



import com.skybooker.auth.model.User;

public interface JwtService {

    String generateToken(User user);

    String extractUsername(String token);

    boolean validateToken(String token);
}