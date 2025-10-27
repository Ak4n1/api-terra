package com.ak4n1.terra.api.terra_api.auth.services;

import com.ak4n1.terra.api.terra_api.auth.dto.RegisterRequestDTO;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AuthService {

    ResponseEntity<?> save(RegisterRequestDTO registerRequest);

    Map<String, Object> sendPasswordResetEmail(String email);

    Map<String, Object> resetPassword(String tokenUser, String newPassword);

    Map<String, String> resendVerificationEmail(String email);

    ResponseEntity<?> verifyEmail(String token);
    
    Map<String, Object> getCurrentUser(String email);

}
