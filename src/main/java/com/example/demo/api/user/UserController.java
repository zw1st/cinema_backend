package com.example.demo.api.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.UserService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/1.0/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Получение профиля текущего пользователя.
     * userId извлекается из SecurityContext (установлен в FirebaseAuthFilter).
     */
    @GetMapping("/me")
    public UserRs getProfile(
            @AuthenticationPrincipal Long userId) {
        UserRs response = userService.getProfile(userId);
        return response;
    }

    /**
     * Обновление профиля (имя, аватар).
     * Пароли не меняются — это делает только через firebase_ui_auth на клиенте.
     */
    @PutMapping("/me")
    public UserRs updateProfile(
            @AuthenticationPrincipal @PathVariable Long userId,
            @RequestBody @Valid UpdateUserRq rq) {
        UserRs response = userService.updateProfile(userId, rq);
        return response;
    }
}