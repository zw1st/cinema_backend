package com.example.demo.service;

import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.userGiftCard.UserGiftCardRq;
import com.example.demo.api.userGiftCard.UserGiftCardRs;
import com.example.demo.entity.GiftCardEntity;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.UserGiftCardEntity;
import com.example.demo.entity.enumeration.GiftCardStatus;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.GiftCardRepository;
import com.example.demo.repository.UserGiftCardRepository;
import com.example.demo.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserGiftCardService {

    private final OrderRepository orderRepository;
    private final UserGiftCardRepository userGiftCardRepository;
    private final GiftCardRepository giftCardRepository;
    private final UserRepository userRepository;

    public UserGiftCardService(UserGiftCardRepository userGiftCardRepository,
            GiftCardRepository giftCardRepository,
            UserRepository userRepository, OrderRepository orderRepository) {
        this.userGiftCardRepository = userGiftCardRepository;
        this.giftCardRepository = giftCardRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    // 🔹 Mock-покупка: мгновенное создание карты
    public UserGiftCardRs purchase(Long buyerId, UserGiftCardRq rq) {
        UserEntity buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new NotFoundException("Buyer not found"));
        GiftCardEntity template = giftCardRepository.findByIdAndIsActiveTrue(rq.giftcardId())
                .orElseThrow(() -> new NotFoundException("Gift card type not available"));

        UserGiftCardEntity card = new UserGiftCardEntity();
        card.setGiftCard(template);
        card.setBuyer(buyer);
        card.setRecipientEmail(rq.recipientEmail().toLowerCase().trim());
        card.setPurchasedAt(LocalDateTime.now());
        card.setExpireDate(LocalDate.now().plusMonths(12));
        card.setStatus(GiftCardStatus.purchased);
        // owner = null (ожидает активации)

        return UserGiftCardRs.from(userGiftCardRepository.save(card));
    }

    // 🔹 Просмотр карт текущим владельцем
    public List<UserGiftCardRs> getCardsByOwner(Long ownerId) {
        return userGiftCardRepository.findByOwnerId(ownerId).stream()
                .map(UserGiftCardRs::from)
                .toList();
    }

    // 🔹 Привязка карт к аккаунту после регистрации/входа
    @Transactional
    public void activateForUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String email = user.getEmail().toLowerCase().trim();
        List<UserGiftCardEntity> pendingCards = userGiftCardRepository
                .findByRecipientEmailAndStatus(email, GiftCardStatus.purchased);

        if (pendingCards.isEmpty())
            return;

        for (UserGiftCardEntity card : pendingCards) {
            if (card.getOwner() == null) {
                card.setOwner(user);
            }
        }
        userGiftCardRepository.saveAll(pendingCards);
    }

    // 🔹 Доступные карты для оплаты заказа (используется в OrderService)
    public List<UserGiftCardRs> getAvailableForPayment(Long ownerId) {
        return userGiftCardRepository.findByOwnerIdAndStatusAndExpireDateAfter(
                ownerId, GiftCardStatus.purchased, LocalDate.now())
                .stream()
                .map(UserGiftCardRs::from)
                .toList();
    }

    public List<UserGiftCardRs> getAllCards() {
        return userGiftCardRepository.findAll().stream()
                .map(UserGiftCardRs::from)
                .toList();
    }

    public List<UserGiftCardRs> getCardsByStatus(GiftCardStatus status) {
        return userGiftCardRepository.findByStatus(status).stream()
                .map(UserGiftCardRs::from)
                .toList();
    }

    public UserGiftCardEntity validateCard(Long cardId, Long userId) {
        UserGiftCardEntity card = userGiftCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Gift card not found"));

        // Проверка владения: карта принадлежит пользователю ИЛИ отправлена на его email
        boolean isOwner = (card.getOwner() != null && card.getOwner().getId().equals(userId)) ||
                (card.getOwner() == null && card.getRecipientEmail().equalsIgnoreCase(
                        userRepository.findById(userId).orElseThrow().getEmail()));

        if (!isOwner) {
            throw new ValidationException("Gift card does not belong to you");
        }
        if (card.getStatus() != GiftCardStatus.purchased) {
            throw new ValidationException("Gift card is not available for use");
        }
        if (card.getExpireDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Gift card has expired");
        }
        return card;
    }

    @Transactional
    public void activateCard(Long cardId, Long orderId) {
        UserGiftCardEntity card = userGiftCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Gift card not found"));

        if (card.getStatus() != GiftCardStatus.purchased) {
            throw new ValidationException("Gift card cannot be activated");
        }

        card.setStatus(GiftCardStatus.activated);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        card.setStatus(GiftCardStatus.activated);
        card.setAppliedToOrder(order);
        userGiftCardRepository.save(card);
    }

    @Transactional
    public void reactivateCard(Long cardId) {
        UserGiftCardEntity card = userGiftCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Gift card not found"));

        // Разрешаем возврат только если карта действительно была активирована
        if (card.getStatus() != GiftCardStatus.activated) {
            throw new ValidationException("Gift card cannot be reactivated: current status is %s"
                    .formatted(card.getStatus()));
        }

        card.setStatus(GiftCardStatus.purchased);
        // appliedToOrder можно обнулить, если нужна обратная связь
        userGiftCardRepository.save(card);
    }

    public UserGiftCardEntity getCardById(Long cardId) {
        return userGiftCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Gift card not found"));
    }
}