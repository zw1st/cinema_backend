package com.example.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.api.order.CreateOrderRq;
import com.example.demo.api.order.OrderRs;
import com.example.demo.api.order.SeatCoordRq;
import com.example.demo.api.ticket.TicketRs;
import com.example.demo.configuration.AppProperties;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.*;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.enumeration.DiscountType;
import com.example.demo.entity.enumeration.OrderStatus;
import com.example.demo.entity.enumeration.TicketStatus;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.SessionRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.UserGiftCardRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OrderService;
import com.example.demo.service.SpecialStatusRequestService;
import com.example.demo.service.UserGiftCardService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    TicketRepository ticketRepository;
    @Mock
    SessionRepository sessionRepository;
    @Mock
    SeatRepository seatRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    SpecialStatusRequestService specialStatusRequestService;
    @Mock
    UserGiftCardService userGiftCardService;
    @Mock
    AppProperties appProperties;
    @Mock
    UserGiftCardRepository userGiftCardRepository;

    @InjectMocks
    OrderService orderService;

    @Captor
    ArgumentCaptor<OrderEntity> orderCaptor;
    @Captor
    ArgumentCaptor<List<TicketEntity>> ticketsCaptor;

    private static final Long USER_ID = 42L;
    private static final Long SESSION_ID = 1L;
    private static final Long HALL_ID = 7L;
    private static final Long CARD_ID = 100L;
    private static final Long ORDER_ID = 500L;
    private static final ZoneId ZONE = ZoneId.of("UTC");

    @Test
    void createOrder_withFreeSeatsAndNoDiscount_createsPendingOrderWithCorrectPrice() {
        // given
        UserEntity user = new UserEntity("user@test.com", "firebase-uid", LocalDateTime.now(), "User", null);
        user.setId(USER_ID);

        HallEntity hall = new HallEntity("Hall 1", 10, 10);
        hall.setId(HALL_ID);

        MovieEntity movie = new MovieEntity("Test Movie", LocalDate.of(2025, 1, 1), true, 8.0,
                (short) 120, "16+", "Description", "http://poster.url", "Drama", "Actors", "Director");

        SessionEntity session = new SessionEntity(movie, hall,
                LocalDate.now().plusDays(1), LocalTime.of(18, 0), LocalTime.of(20, 0),
                new BigDecimal("400"));
        session.setId(SESSION_ID);

        SeatType standard = new SeatType("STANDARD", new BigDecimal("1.0"));
        SeatType vip = new SeatType("VIP", new BigDecimal("1.5"));

        SeatEntity seat1 = new SeatEntity(1, 5, standard, hall);
        SeatEntity seat2 = new SeatEntity(1, 6, vip, hall);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(seatRepository.findByHallIdAndRowNumAndColNum(HALL_ID, 1, 5)).thenReturn(Optional.of(seat1));
        when(seatRepository.findByHallIdAndRowNumAndColNum(HALL_ID, 1, 6)).thenReturn(Optional.of(seat2));
        // existsBy...StatusIn вернёт false, а findBy...CANCELLED вернёт
        // Optional.empty()
        // по умолчанию, поэтому отдельные стабы не нужны

        CreateOrderRq rq = new CreateOrderRq(SESSION_ID, null, null,
                List.of(new SeatCoordRq(1, 5), new SeatCoordRq(1, 6)));

        // when
        OrderRs result = orderService.createOrder(rq, USER_ID);

        // then: ответ
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.discountType()).isEqualTo("NONE");
        assertThat(result.totalAmount()).isEqualByComparingTo("1000"); // 400 * 1.0 + 400 * 1.5
        assertThat(result.finalPrice()).isEqualByComparingTo("1000"); // без скидки равна totalAmount
        // assertThat(result.statusDiscountCoef()).isNull();
        assertThat(result.appliedGiftCardId()).isNull();
        assertThat(result.exchangeFromOrderId()).isNull();

        assertThat(result.tickets()).hasSize(2);
        assertThat(result.tickets()).extracting(TicketRs::status).containsOnly("RESERVED");
        assertThat(result.tickets()).extracting(TicketRs::finalPrice)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("400"), new BigDecimal("600"));

        // then: что сохранено в базу
        verify(orderRepository).save(orderCaptor.capture());
        OrderEntity savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getUser()).isSameAs(user);
        assertThat(savedOrder.getCustomerEmail()).isEqualTo("user@test.com");
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);

        verify(ticketRepository).saveAll(ticketsCaptor.capture());
        assertThat(ticketsCaptor.getValue()).hasSize(2)
                .allSatisfy(t -> {
                    assertThat(t.getOrder()).isSameAs(savedOrder);
                    assertThat(t.getSession()).isSameAs(session);
                    assertThat(t.getStatus()).isEqualTo(TicketStatus.RESERVED);
                    assertThat(t.getTicketCode()).isNull();
                });

        // then: сервисы скидок не должны вызываться
        verifyNoInteractions(specialStatusRequestService, userGiftCardService, userGiftCardRepository);
    }

    private UserEntity givenUserSessionAndSingleSeat(BigDecimal basePrice) {
        UserEntity user = new UserEntity("user@test.com", "firebase-uid", LocalDateTime.now(), "User", null);
        user.setId(USER_ID);

        HallEntity hall = new HallEntity("Hall 1", 10, 10);
        hall.setId(HALL_ID);

        MovieEntity movie = new MovieEntity("Test Movie", LocalDate.of(2025, 1, 1), true, 8.0,
                (short) 120, "16+", "Description", "http://poster.url", "Drama", "Actors", "Director");

        SessionEntity session = new SessionEntity(movie, hall,
                LocalDate.now().plusDays(1), LocalTime.of(18, 0), LocalTime.of(20, 0), basePrice);
        session.setId(SESSION_ID);

        SeatEntity seat = new SeatEntity(1, 5, new SeatType("STANDARD", new BigDecimal("1.0")), hall);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(seatRepository.findByHallIdAndRowNumAndColNum(HALL_ID, 1, 5)).thenReturn(Optional.of(seat));
        return user;
    }

    /**
     * Готовит карту с заданным номиналом: и валидация, и поиск номинала возвращают
     * её.
     */
    private void givenGiftCard(UserEntity user, BigDecimal nominal) {
        GiftCardEntity giftCard = new GiftCardEntity(nominal, true);

        UserGiftCardEntity userCard = new UserGiftCardEntity(giftCard, user, user, null,
                LocalDateTime.now(), LocalDate.now().plusYears(1), null, null);
        userCard.setId(CARD_ID);

        when(userGiftCardService.validateCard(CARD_ID, USER_ID)).thenReturn(userCard);
        when(userGiftCardRepository.findById(CARD_ID)).thenReturn(Optional.of(userCard));
    }

    @Test
    void createOrder_withGiftCard_subtractsNominalFromFinalPrice() {
        // given: билет 1500, карта на 1000
        UserEntity user = givenUserSessionAndSingleSeat(new BigDecimal("1500"));
        givenGiftCard(user, new BigDecimal("1000"));

        CreateOrderRq rq = new CreateOrderRq(SESSION_ID, false, CARD_ID, List.of(new SeatCoordRq(1, 5)));

        // when
        OrderRs result = orderService.createOrder(rq, USER_ID);

        // then: ответ
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.discountType()).isEqualTo("GIFT_CARD");
        assertThat(result.appliedGiftCardId()).isEqualTo(CARD_ID);
        assertThat(result.statusDiscountCoef()).isEqualTo(new BigDecimal(1));
        assertThat(result.totalAmount()).isEqualByComparingTo("1500"); // сумма по местам не меняется
        assertThat(result.finalPrice()).isEqualByComparingTo("500"); // 1500 - 1000

        // then: в базу ушёл заказ с привязанной картой
        verify(orderRepository).existsByAppliedGiftCardIdAndStatus(CARD_ID, OrderStatus.PENDING);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getAppliedGiftCardId()).isEqualTo(CARD_ID);
        assertThat(orderCaptor.getValue().getDiscountType()).isEqualTo(DiscountType.GIFT_CARD);

        verifyNoInteractions(specialStatusRequestService);
    }

    @Test
    void createOrder_whenGiftCardNominalExceedsTotal_finalPriceIsZeroNotNegative() {
        // given: билет 400, карта на 1000 (400 - 1000 = -600, ожидаем 0)
        UserEntity user = givenUserSessionAndSingleSeat(new BigDecimal("400"));
        givenGiftCard(user, new BigDecimal("1000"));

        CreateOrderRq rq = new CreateOrderRq(SESSION_ID, false, CARD_ID, List.of(new SeatCoordRq(1, 5)));

        // when
        OrderRs result = orderService.createOrder(rq, USER_ID);

        // then
        assertThat(result.discountType()).isEqualTo("GIFT_CARD");
        assertThat(result.totalAmount()).isEqualByComparingTo("400");
        assertThat(result.finalPrice()).isEqualByComparingTo("0");
        assertThat(result.finalPrice().signum()).isGreaterThanOrEqualTo(0);

        verify(orderRepository).save(any(OrderEntity.class));
    }

    private void givenRefundPolicy() {
        when(appProperties.getTimezone()).thenReturn(ZONE);
        when(appProperties.getNoRefundBeforeSession()).thenReturn(60);
    }

    /**
     * Сеанс завтра в 18:00: возврат разрешён. Фильм и зал при возврате не читаются.
     */
    private SessionEntity farSession() {
        return new SessionEntity(null, null, LocalDate.now(ZONE).plusDays(1),
                LocalTime.of(18, 0), LocalTime.of(20, 0), new BigDecimal("1000"));
    }

    /** Сеанс через 30 минут при запрете возврата за 60: возврат закрыт. */
    private SessionEntity startingSoonSession() {
        LocalDateTime start = LocalDateTime.now(ZONE).plusMinutes(30);
        return new SessionEntity(null, null, start.toLocalDate(), start.toLocalTime(),
                start.toLocalTime().plusHours(2), new BigDecimal("1000"));
    }

    /** Оплаченный заказ с картой. Суммы каждый тест задаёт сам. */
    private OrderEntity paidOrderWithGiftCard(String total, String finalPrice) {
        OrderEntity order = new OrderEntity();
        order.setId(ORDER_ID);
        order.setStatus(OrderStatus.PAID);
        order.setDiscountType(DiscountType.GIFT_CARD);
        order.setAppliedGiftCardId(CARD_ID);
        order.setTotalAmount(new BigDecimal(total));
        order.setFinalPrice(new BigDecimal(finalPrice));
        when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        return order;
    }

    private TicketEntity paidTicket(Long id, OrderEntity order, SessionEntity session, String price) {
        TicketEntity t = new TicketEntity();
        t.setId(id);
        t.setOrder(order);
        t.setSession(session);
        t.setStatus(TicketStatus.PAID);
        t.setFinalPrice(new BigDecimal(price));
        return t;
    }

    /**
     * Только поиск номинала: validateCard при возврате не вызывается, стаб на него
     * был бы лишним.
     */
    private void givenCardNominal(String nominal) {
        UserEntity user = new UserEntity("user@test.com", "firebase-uid", LocalDateTime.now(), "User", null);
        UserGiftCardEntity userCard = new UserGiftCardEntity(new GiftCardEntity(new BigDecimal(nominal), true),
                user, user, null, LocalDateTime.now(), LocalDate.now().plusYears(1), null, null);
        userCard.setId(CARD_ID);
        when(userGiftCardRepository.findById(CARD_ID)).thenReturn(Optional.of(userCard));
    }

    @Test
    void refundTickets_partialRefundWithGiftCard_recalculatesPriceAndKeepsCardApplied() {
        // given: 3 билета по 1000, карта на 1000, заплачено 2000. Возвращаем билет №1
        givenRefundPolicy();
        givenCardNominal("1000");
        OrderEntity order = paidOrderWithGiftCard("3000", "2000");
        SessionEntity session = farSession();
        List<TicketEntity> refunded = List.of(paidTicket(1L, order, session, "1000"));
        List<TicketEntity> remaining = List.of(
                paidTicket(2L, order, session, "1000"),
                paidTicket(3L, order, session, "1000"));

        when(ticketRepository.findByIdIn(List.of(1L))).thenReturn(refunded);
        when(ticketRepository.findByOrderIdAndStatusIn(ORDER_ID, List.of(TicketStatus.PAID, TicketStatus.SCANNED)))
                .thenReturn(remaining);

        // when
        orderService.refundTickets(ORDER_ID, List.of(1L), USER_ID);

        // then: билет освобождён
        assertThat(refunded.get(0).getStatus()).isEqualTo(TicketStatus.CANCELLED);
        verify(ticketRepository).saveAll(refunded);

        // then: цена пересчитана от оставшихся билетов, номинал вычтен заново
        assertThat(order.getTotalAmount()).isEqualByComparingTo("2000");
        assertThat(order.getFinalPrice()).isEqualByComparingTo("1000"); // 2000 - 1000

        // then: карта остаётся на заказе, заказ жив
        assertThat(order.getAppliedGiftCardId()).isEqualTo(CARD_ID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(userGiftCardService, never()).reactivateCard(any());
        verify(orderRepository).save(order);
    }

    @Test
    void refundTickets_whenRemainingTotalBelowNominal_finalPriceIsZeroNotNegative() {
        // given: билеты 1000 и 400, карта на 1000. Возвращаем билет на 1000, остаётся
        // 400
        givenRefundPolicy();
        givenCardNominal("1000");
        OrderEntity order = paidOrderWithGiftCard("1400", "400");
        SessionEntity session = farSession();
        List<TicketEntity> refunded = List.of(paidTicket(1L, order, session, "1000"));
        List<TicketEntity> remaining = List.of(paidTicket(2L, order, session, "400"));

        when(ticketRepository.findByIdIn(List.of(1L))).thenReturn(refunded);
        when(ticketRepository.findByOrderIdAndStatusIn(ORDER_ID, List.of(TicketStatus.PAID, TicketStatus.SCANNED)))
                .thenReturn(remaining);

        // when
        orderService.refundTickets(ORDER_ID, List.of(1L), USER_ID);

        // then: 400 - 1000 = -600, ожидаем 0
        assertThat(order.getTotalAmount()).isEqualByComparingTo("400");
        assertThat(order.getFinalPrice()).isEqualByComparingTo("0");
        assertThat(order.getFinalPrice().signum()).isGreaterThanOrEqualTo(0);
        assertThat(order.getAppliedGiftCardId()).isEqualTo(CARD_ID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void refundTickets_fullRefundWithGiftCard_reactivatesCardAndCancelsOrder() {
        // given: два билета, список id не передан, значит возвращаются все оплаченные
        givenRefundPolicy();
        givenCardNominal("1000");
        OrderEntity order = paidOrderWithGiftCard("2000", "1000");
        SessionEntity session = farSession();
        List<TicketEntity> all = List.of(
                paidTicket(1L, order, session, "1000"),
                paidTicket(2L, order, session, "1000"));

        when(ticketRepository.findByOrderIdAndStatus(ORDER_ID, TicketStatus.PAID)).thenReturn(all);
        // findByOrderIdAndStatusIn не стабим: Mockito вернёт пустой список, то есть
        // билетов не осталось

        // when
        orderService.refundTickets(ORDER_ID, null, USER_ID);

        // then: билеты возвращены
        assertThat(all).extracting(TicketEntity::getStatus).containsOnly(TicketStatus.CANCELLED);

        // then: карта возвращена владельцу и отвязана от заказа
        verify(userGiftCardService).reactivateCard(CARD_ID);
        assertThat(order.getAppliedGiftCardId()).isNull();

        // then: заказ отменён, суммы обнулены
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("0");
        assertThat(order.getFinalPrice()).isEqualByComparingTo("0");
        verify(orderRepository).save(order);
    }

    @Test
    void refundTickets_tooCloseToSession_throwsAndLeavesOrderAndCardUntouched() {
        // given: до сеанса 30 минут, а возврат закрывается за 60
        givenRefundPolicy();
        OrderEntity order = paidOrderWithGiftCard("1000", "0");
        TicketEntity ticket = paidTicket(1L, order, startingSoonSession(), "1000");
        when(ticketRepository.findByIdIn(List.of(1L))).thenReturn(List.of(ticket));

        // when / then
        assertThatThrownBy(() -> orderService.refundTickets(ORDER_ID, List.of(1L), USER_ID))
                .isInstanceOf(ValidationException.class);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PAID);
        assertThat(order.getAppliedGiftCardId()).isEqualTo(CARD_ID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(ticketRepository, never()).saveAll(any());
        verify(orderRepository, never()).save(any());
        verify(userGiftCardService, never()).reactivateCard(any());
    }

}