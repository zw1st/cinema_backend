package com.example.demo.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.user.SignInRq;
import com.example.demo.api.user.SignUpRq;
import com.example.demo.api.user.UserRs;
import com.example.demo.exception.InvalidTokenException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

@Service
@Profile("prod")
public class AuthService {
    private final FirebaseAuth firebaseAuth;
    private final UserService userService;
    private final UserGiftCardService userGiftCardService;

    public AuthService(FirebaseAuth firebaseAuth, UserService userService, UserGiftCardService userGiftCardService) {
        this.firebaseAuth = firebaseAuth;
        this.userService = userService;
        this.userGiftCardService = userGiftCardService;
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
        UserRs createdUser = userService.syncUser(rq);
        userGiftCardService.activateForUser(userService.getEntityByFirebaseUid(rq.firebaseUid()).getId());
        return createdUser;
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