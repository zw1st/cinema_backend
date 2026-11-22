package com.example.demo.api.user;

import jakarta.validation.Valid;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.AuthService;

@RestController
@RequestMapping("/auth")
@Profile("prod")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Синхронизация пользователя после входа через Firebase.
     * Публичный эндпоинт: вызывается сразу после получения idToken на клиенте.
     * Тело запроса содержит данные для поиска/создания, но доверие — только к
     * токену в заголовке.
     */
    @PostMapping("/sync")
    public UserRs syncUser(
            @RequestHeader("Authorization") String idToken,
            @RequestBody @Valid SignInRq rq) {
        // Убираем префикс "Bearer ", если он есть (клиент может отправить с ним или
        // без)
        String token = idToken.startsWith("Bearer ") ? idToken.substring(7) : idToken;

        UserRs response = authService.authenticateSignIn(token, rq);

        return response;
    }

    /**
     * Алиас для /sync. Семантически отражает "первую регистрацию".
     * Логика идентична, так как и вход, и регистрация в Firebase возвращают
     * idToken.
     */
    @PostMapping("/sign-up")
    public UserRs signUp(
            @RequestHeader("Authorization") String idToken,
            @RequestBody @Valid SignUpRq rq) {
        String token = idToken.startsWith("Bearer ") ? idToken.substring(7) : idToken;

        UserRs response = authService.authenticateSignUp(token, rq);
        return response;
    }
}