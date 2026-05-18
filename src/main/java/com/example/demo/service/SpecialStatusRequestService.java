package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.userStatusRequest.CreateStatusRequestRq;
import com.example.demo.api.userStatusRequest.StatusRequestRs;
import com.example.demo.entity.SpecialStatusEntity;
import com.example.demo.entity.SpecialStatusUserEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.enumeration.UserStatusRequestCondition;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.SpecialStatusRepository;
import com.example.demo.repository.SpecialStatusUserRepository;
import com.example.demo.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class SpecialStatusRequestService {

    private final SpecialStatusUserRepository requestRepository;
    private final SpecialStatusRepository statusRepository;
    private final UserRepository userRepository;

    public SpecialStatusRequestService(SpecialStatusUserRepository requestRepository,
            SpecialStatusRepository statusRepository,
            UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.statusRepository = statusRepository;
        this.userRepository = userRepository;
    }

    // 🔹 Пользователь: создание заявки
    @Transactional
    public StatusRequestRs createRequest(Long userId, CreateStatusRequestRq rq) {
        SpecialStatusEntity status = statusRepository.findById(rq.statusId())
                .orElseThrow(() -> new NotFoundException("Status type not found"));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        SpecialStatusUserEntity request = new SpecialStatusUserEntity();
        request.setUser(user);
        request.setSpecialStatus(status);
        request.setRequestedAt(LocalDateTime.now());
        request.setStatus(UserStatusRequestCondition.pending);
        request.setExpireDate(rq.expireDate()); // Пользователь указывает срок действия документа
        // grantedAt остаётся null до одобрения

        return StatusRequestRs.from(requestRepository.save(request));
    }

    // 🔹 Пользователь: просмотр своих заявок
    public List<StatusRequestRs> getUserRequests(Long userId) {
        return requestRepository.findByUserId(userId).stream()
                .map(StatusRequestRs::from)
                .toList();
    }

    // 🔹 Админ: просмотр заявок в ожидании
    public List<StatusRequestRs> getPendingRequests() {
        return requestRepository.findByStatus(UserStatusRequestCondition.pending).stream()
                .map(StatusRequestRs::from)
                .toList();
    }

    @Transactional
    public StatusRequestRs approveRequest(Long requestId) {
        SpecialStatusUserEntity req = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        if (req.getStatus() != UserStatusRequestCondition.pending) {
            throw new ValidationException("Only pending requests can be approved");
        }
        req.setStatus(UserStatusRequestCondition.active);
        req.setGrantedAt(LocalDate.now());
        // expireDate оставляем из заявки пользователя (админ может скорректировать
        // позже при необходимости)
        return StatusRequestRs.from(requestRepository.save(req));
    }

    @Transactional
    public StatusRequestRs declineRequest(Long requestId) {
        SpecialStatusUserEntity req = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        if (req.getStatus() != UserStatusRequestCondition.pending) {
            throw new ValidationException("Only pending requests can be declined");
        }
        req.setStatus(UserStatusRequestCondition.declined);
        return StatusRequestRs.from(requestRepository.save(req));
    }

    public Optional<SpecialStatusUserEntity> getMaxDiscountActiveStatus(Long userId) {
        return requestRepository.findByUserIdAndStatus(userId, UserStatusRequestCondition.active).stream()
                // Фильтруем истекшие статусы (если expireDate не указан или еще не наступил)
                .filter(req -> req.getStatus() == UserStatusRequestCondition.active)
                // Ищем запись с наибольшим коэффициентом скидки
                .min(Comparator.comparing(req -> req.getSpecialStatus().getDiscountSize()));
    }
}