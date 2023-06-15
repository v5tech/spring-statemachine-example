package net.ameizi.spring.statemachine.enums;

/**
 * 订单事件
 *
 */
public enum OrderStateChangeEvent {
    // 支付，发货，确认收货
    PAYED, DELIVERY, RECEIVED;
}
