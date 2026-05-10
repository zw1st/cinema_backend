package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.user.SignInRq;
import com.example.demo.api.user.SignUpRq;
import com.example.demo.api.user.UserRs;
import com.example.demo.exception.InvalidTokenException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

@Service
public class AuthService {
    private final FirebaseAuth firebaseAuth;
    private final UserService userService;

    public AuthService(FirebaseAuth firebaseAuth, UserService userService) {
        this.firebaseAuth = firebaseAuth;
        this.userService = userService;
    }

    /**
     * Верифицирует ID токен через Firebase Admin SDK.
     * При истекшем, просроченном или поддельном токене бросает исключение.
     */
    public FirebaseToken verifyToken(String idToken) {
        try {
            return firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new InvalidTokenException("Invalid Firebase token", e);
        }
    }

    @Transactional
    public UserRs authenticateSignIn(String idToken, SignInRq rq) {
        validateToken(idToken, rq.firebaseUid());
        return userService.syncUser(rq);
    }

    @Transactional
    public UserRs authenticateSignUp(String idToken, SignUpRq rq) {
        validateToken(idToken, rq.firebaseUid());
        // Логика идентична, маппим во внутренний DTO для UserService
        return userService.syncUser(new SignInRq(rq.firebaseUid(), rq.email(), rq.name()));
    }

    private void validateToken(String idToken, String expectedUid) {
        FirebaseToken decoded = verifyToken(idToken);
        if (!decoded.getUid().equals(expectedUid)) {
            throw new InvalidTokenException("Firebase UID mismatch");
        }
    }

}