package com.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

import com.example.demo.entity.SpecialStatusEntity;
import com.example.demo.entity.SpecialStatusUserEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.enumeration.UserStatusRequestCondition;
import com.example.demo.repository.SpecialStatusUserRepository;
import com.example.demo.utils.SpecialStatusExpirationScheduler;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SpecialStatusSchedulerTest {
    @Mock
    SpecialStatusUserRepository requestRepository;

    @InjectMocks
    SpecialStatusExpirationScheduler scheduler;

    private SpecialStatusUserEntity activeStatus(LocalDate expireDate) {
        UserEntity user = new UserEntity("student@test.com", "firebase-uid", LocalDateTime.now(), "Student", null);
        SpecialStatusEntity student = new SpecialStatusEntity("Student", true, new BigDecimal("0.8"));

        return new SpecialStatusUserEntity(student, user, LocalDateTime.now().minusMonths(6),
                LocalDate.now().minusMonths(6), expireDate, UserStatusRequestCondition.active);
    }

    @Test
    void expireOutdatedStatuses_expiresOnlyStatusesWhoseDateHasCome() {
        // given
        LocalDate today = LocalDate.now();
        SpecialStatusUserEntity expiredYesterday = activeStatus(today.minusDays(1));
        SpecialStatusUserEntity expiresToday = activeStatus(today);
        SpecialStatusUserEntity validUntilTomorrow = activeStatus(today.plusDays(1));
        SpecialStatusUserEntity unlimited = activeStatus(null);

        List<SpecialStatusUserEntity> active = List.of(
                expiredYesterday, expiresToday, validUntilTomorrow, unlimited);
        when(requestRepository.findByStatus(UserStatusRequestCondition.active)).thenReturn(active);

        // when
        scheduler.expireOutdatedStatuses();

        // then: истекли только те, у кого дата наступила
        assertThat(expiredYesterday.getStatus()).isEqualTo(UserStatusRequestCondition.expired);
        assertThat(expiresToday.getStatus()).isEqualTo(UserStatusRequestCondition.expired);

        // then: остальные не тронуты
        assertThat(validUntilTomorrow.getStatus()).isEqualTo(UserStatusRequestCondition.active);
        assertThat(unlimited.getStatus()).isEqualTo(UserStatusRequestCondition.active);

        verify(requestRepository).saveAll(active);
    }

    @Test
    void expireOutdatedStatuses_whenNothingExpired_doesNotSave() {
        SpecialStatusUserEntity validUntilTomorrow = activeStatus(LocalDate.now().plusDays(1));
        SpecialStatusUserEntity unlimited = activeStatus(null);
        when(requestRepository.findByStatus(UserStatusRequestCondition.active))
                .thenReturn(List.of(validUntilTomorrow, unlimited));

        scheduler.expireOutdatedStatuses();

        assertThat(validUntilTomorrow.getStatus()).isEqualTo(UserStatusRequestCondition.active);
        assertThat(unlimited.getStatus()).isEqualTo(UserStatusRequestCondition.active);
        verify(requestRepository, never()).saveAll(any());
    }
}
