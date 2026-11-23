package com.example.demo;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.api.userStatus.UserDiscountRs;
import com.example.demo.entity.SpecialStatusEntity;
import com.example.demo.entity.SpecialStatusUserEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.enumeration.UserStatusRequestCondition;
import com.example.demo.repository.SpecialStatusRepository;
import com.example.demo.repository.SpecialStatusUserRepository;
import com.example.demo.service.SpecialStatusService;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SpecialStatusTest {
    @Mock
    SpecialStatusRepository statusRepository;
    @Mock
    SpecialStatusUserRepository requestRepository;

    @InjectMocks
    SpecialStatusService service;

    private static final Long USER_ID = 42L;

    private SpecialStatusUserEntity activeStatus(String name, BigDecimal coef) {
        UserEntity user = new UserEntity("student@test.com", "firebase-uid", LocalDateTime.now(), "Student", null);
        SpecialStatusEntity status = new SpecialStatusEntity(name, true, coef);
        return new SpecialStatusUserEntity(status, user, LocalDateTime.now().minusMonths(1),
                LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(5),
                UserStatusRequestCondition.active);
    }

    @Test
    void getBestDiscount_withSeveralActiveStatuses_returnsSmallestCoefficient() {
        BigDecimal best = new BigDecimal("0.5");
        when(requestRepository.findByUserIdAndStatus(USER_ID, UserStatusRequestCondition.active))
                .thenReturn(List.of(
                        activeStatus("Pensioner", new BigDecimal("0.9")),
                        activeStatus("Disabled", best),
                        activeStatus("Student", new BigDecimal("0.8"))));

        UserDiscountRs result = service.getBestDiscount(USER_ID);

        assertThat(result).isEqualTo(new UserDiscountRs(true, best));
    }

    @Test
    void getBestDiscount_withSingleActiveStatus_returnsItsCoefficient() {
        BigDecimal coef = new BigDecimal("0.8");
        when(requestRepository.findByUserIdAndStatus(USER_ID, UserStatusRequestCondition.active))
                .thenReturn(List.of(activeStatus("Student", coef)));

        assertThat(service.getBestDiscount(USER_ID)).isEqualTo(new UserDiscountRs(true, coef));
    }

    @Test
    void getBestDiscount_withoutActiveStatuses_returnsNoDiscount() {
        when(requestRepository.findByUserIdAndStatus(USER_ID, UserStatusRequestCondition.active))
                .thenReturn(List.of());

        assertThat(service.getBestDiscount(USER_ID)).isEqualTo(new UserDiscountRs(false, BigDecimal.ONE));
    }
}
