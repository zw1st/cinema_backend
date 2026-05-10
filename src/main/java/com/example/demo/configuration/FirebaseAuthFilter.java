package com.example.demo.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.exception.InvalidTokenException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final UserService userService;

    // Эндпоинты, доступные без токена (синхронизация при первом входе)
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/sync",
            "/auth/sign-up",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/h2-console/**",
            "/webjars/**");

    public FirebaseAuthFilter(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    // @Override
    // protected void doFilterInternal(
    // @NonNull HttpServletRequest request,
    // @NonNull HttpServletResponse response,
    // @NonNull FilterChain filterChain) throws ServletException, IOException {

    // System.out.println(
    // "🔍 Filter: path=" + request.getRequestURI() + " authHeader=" +
    // request.getHeader("Authorization"));
    // String path = request.getRequestURI();

    // // 🔹 1. Пропускаем публичные эндпоинты
    // if (isPublicPath(path)) {
    // filterChain.doFilter(request, response);
    // return;
    // }

    // // 🔹 2. Извлекаем токен из заголовка
    // String token = extractToken(request);
    // if (token == null) {
    // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing
    // Authorization header");
    // return;
    // }

    // try {
    // // 🔹 3. Верифицируем токен и извлекаем UID (доверяем только токену!)
    // var decodedToken = authService.verifyToken(token);
    // String firebaseUid = decodedToken.getUid();

    // // 🔹 4. Находим пользователя в БД по доверенному UID
    // var userEntity = userService.getEntityByFirebaseUid(firebaseUid);

    // // 🔹 5. Создаём аутентификацию и кладём в контекст
    // var authentication = new UsernamePasswordAuthenticationToken(
    // userEntity.getId(), // Principal = внутренний ID (Long)
    // null, // Credentials не нужны после верификации
    // Collections.emptyList() // Authorities (роли) можно добавить позже
    // );
    // authentication.setDetails(new
    // WebAuthenticationDetailsSource().buildDetails(request));
    // SecurityContextHolder.getContext().setAuthentication(authentication);

    // } catch (InvalidTokenException e) {
    // // Токен просрочен, подделан или отозван
    // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
    // return;
    // } catch (NotFoundException e) {
    // // Токен валиден, но пользователя нет в БД (не прошла синхронизация)
    // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not
    // synchronized");
    // return;
    // } catch (Exception e) {
    // // Любая другая ошибка — 500
    // logger.error("Unexpected error in FirebaseAuthFilter", e);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal
    // server error");
    // return;
    // }

    // // 🔹 6. Передаём управление дальше по цепочке
    // filterChain.doFilter(request, response);
    // }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        System.out.println("🛡️ [1/6] Filter invoked: " + path);
        System.out.println("🛡️ [2/6] Auth header: " + (authHeader != null ? "Present" : "MISSING"));

        if (isPublicPath(path)) {
            System.out.println("🛡️ [SKIP] Path is public");
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (token == null) {
            System.out.println("🛡️ [ERROR] Token extraction failed");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header");
            return;
        }
        System.out.println("🛡️ [3/6] Token extracted successfully");

        try {
            System.out.println("🛡️ [4/6] Verifying token with Firebase...");
            var decodedToken = authService.verifyToken(token);
            System.out.println("🛡️ [4/6] Token verified! UID: " + decodedToken.getUid());

            System.out.println("🛡️ [5/6] Looking up user in DB...");
            var userEntity = userService.getEntityByFirebaseUid(decodedToken.getUid());
            System.out.println("🛡️ [5/6] User found! ID: " + userEntity.getId());

            var authentication = new UsernamePasswordAuthenticationToken(
                    userEntity.getId(), null, Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("🛡️ [6/6] SecurityContext SET successfully");

        } catch (InvalidTokenException e) {
            System.err.println("🛡️ [EXCEPTION] InvalidTokenException: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        } catch (NotFoundException e) {
            System.err.println("🛡️ [EXCEPTION] NotFoundException: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not synchronized");
            return;
        } catch (Exception e) {
            // 🔹 Используем System.err вместо logger
            System.err.println("🛡️ [EXCEPTION] Unexpected: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
            return;
        }

        System.out.println("🛡️ [DONE] Passing request to next filter");
        filterChain.doFilter(request, response);
    }

    /**
     * Проверяет, является ли путь публичным (не требует аутентификации).
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(pattern -> {
                    // Убираем /** для сравнения
                    String prefix = pattern.replace("/**", "").replace("/*", "");
                    return path.startsWith(prefix);
                });
    }

    /**
     * Извлекает Bearer-токен из заголовка Authorization.
     * Возвращает null, если заголовок отсутствует или имеет неверный формат.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7); // Убираем префикс "Bearer "
    }
}
