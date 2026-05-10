package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.user.SignInRq;
import com.example.demo.api.user.UpdateUserRq;
import com.example.demo.api.user.UserRs;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UserEntity getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(UserEntity.class, id));
    }

    @Transactional
    public UserEntity getEntityByFirebaseUid(String firebaseUid) {
        return repository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new NotFoundException(UserEntity.class, "firebaseUid", firebaseUid));
    }

    @Transactional(readOnly = true)
    public UserRs getProfile(Long id) {
        final UserEntity entity = getEntity(id);
        return UserRs.from(entity);
    }

    /**
     * Синхронизация пользователя после входа/регистрации через Firebase.
     * Логика: найти по UID → найти по Email → создать нового.
     * Подходит и для SignInRq, и для SignUpRq (структура идентична).
     */
    @Transactional
    public UserRs syncUser(SignInRq rq) {
        // 1. Поиск по Firebase UID (основной идентификатор сессии)
        Optional<UserEntity> byUid = repository.findByFirebaseUid(rq.firebaseUid());
        if (byUid.isPresent()) {
            return UserRs.from(byUid.get());
        }

        // 2. Поиск по Email (если аккаунт уже был, но без привязки к Firebase)
        Optional<UserEntity> byEmail = repository.findByEmail(rq.email());
        if (byEmail.isPresent()) {
            UserEntity entity = byEmail.get();
            entity.setFirebaseUid(rq.firebaseUid());
            if (rq.name() != null && entity.getName() == null) {
                entity.setName(rq.name());
            }
            return UserRs.from(repository.save(entity));
        }

        // 3. Создание новой записи (первый вход)
        UserEntity entity = new UserEntity(rq.email(), rq.firebaseUid(), LocalDateTime.now(), rq.name(), null);
        return UserRs.from(repository.save(entity));
    }

    /**
     * Обновление профиля (имя и аватар).
     * Вызывается только после успешной аутентификации, поэтому берём сущность по
     * trusted id.
     */
    @Transactional
    public UserRs updateProfile(Long userId, UpdateUserRq rq) {
        UserEntity entity = getEntity(userId);

        if (rq.name() != null) {
            entity.setName(rq.name());
        }
        if (rq.avatarUrl() != null) {
            entity.setAvatarUrl(rq.avatarUrl());
        }

        return UserRs.from(repository.save(entity));
    }
}