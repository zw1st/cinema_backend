package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.entity.enumeration.GiftCardStatus;

@Entity
@Table(name = "user_gift_card")
public class UserGiftCardEntity extends BaseEntity {

    @JoinColumn(name = "giftcard_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private GiftCardEntity giftCard;

    @JoinColumn(name = "owner_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity owner; // NULL, пока получатель не зарегистрировался/не вошёл в систему

    @JoinColumn(name = "buyer_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity buyer; // Кто оплатил карту

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail; // Почта получателя (для незарегистрированных)

    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;

    @Column(name = "expire_date", nullable = false)
    private LocalDate expireDate; // purchasedAt + 12 месяцев

    @Enumerated(EnumType.STRING)
    @Column(name = "gift_status", nullable = false, length = 20)
    private GiftCardStatus status;

    @JoinColumn(name = "applied_to_order_id") // Заказ, в котором карта была списана
    @ManyToOne(fetch = FetchType.LAZY)
    private OrderEntity appliedToOrder;

    public UserGiftCardEntity() {
        super();
    }

    public GiftCardEntity getGiftCard() {
        return giftCard;
    }

    public void setGiftCard(GiftCardEntity giftCard) {
        this.giftCard = giftCard;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public UserEntity getBuyer() {
        return buyer;
    }

    public void setBuyer(UserEntity buyer) {
        this.buyer = buyer;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(LocalDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    public GiftCardStatus getStatus() {
        return status;
    }

    public void setStatus(GiftCardStatus status) {
        this.status = status;
    }

    public OrderEntity getAppliedToOrder() {
        return appliedToOrder;
    }

    public void setAppliedToOrder(OrderEntity appliedToOrder) {
        this.appliedToOrder = appliedToOrder;
    }

    public UserGiftCardEntity(GiftCardEntity giftCard, UserEntity owner, UserEntity buyer, String recipientEmail,
            LocalDateTime purchasedAt, LocalDate expireDate, GiftCardStatus status, OrderEntity appliedToOrder) {
        this.giftCard = giftCard;
        this.owner = owner;
        this.buyer = buyer;
        this.recipientEmail = recipientEmail;
        this.purchasedAt = purchasedAt;
        this.expireDate = expireDate;
        this.status = status;
        this.appliedToOrder = appliedToOrder;
    }
}