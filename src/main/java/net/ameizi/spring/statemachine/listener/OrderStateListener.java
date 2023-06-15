package net.ameizi.spring.statemachine.listener;

import lombok.extern.slf4j.Slf4j;
import net.ameizi.spring.statemachine.aop.Transition;
import net.ameizi.spring.statemachine.config.Constants;
import net.ameizi.spring.statemachine.entity.Order;
import net.ameizi.spring.statemachine.enums.OrderState;
import net.ameizi.spring.statemachine.mapper.OrderMapper;
import org.springframework.messaging.Message;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.stereotype.Component;
import net.ameizi.spring.statemachine.enums.OrderStateChangeEvent;

import javax.annotation.Resource;

/**
 * 订单状态变更监听器
 */
@Slf4j
@Component("orderStateListener")
@WithStateMachine(name = "orderStateMachine")
public class OrderStateListener {

    @Resource
    private OrderMapper orderMapper;

    @Transition(key = Constants.PAY_TRANSITION)
    @OnTransition(source = "WAIT_PAYMENT", target = "WAIT_DELIVER")
    public boolean payTransition(Message<OrderStateChangeEvent> message) {
        Order order = (Order) message.getHeaders().get("order");
        log.info("支付，状态机反馈信息：{}", message.getHeaders());
        // 更新订单
        assert order != null;
        order.setStatus(OrderState.WAIT_DELIVER);
        // 持久化到数据库
        orderMapper.saveOrUpdate(order);
        // 模拟业务代码抛出异常
        // int i = 1 / 0;
        // TODO: 其他业务代码
        return true;
    }

    @Transition(key = Constants.DELIVER_TRANSITION)
    @OnTransition(source = "WAIT_DELIVER", target = "WAIT_RECEIVE")
    public boolean deliverTransition(Message<OrderStateChangeEvent> message) {
        Order order = (Order) message.getHeaders().get("order");
        log.info("发货，状态机反馈信息：{}", message.getHeaders());
        // 更新订单
        assert order != null;
        order.setStatus(OrderState.WAIT_RECEIVE);
        // 持久化到数据库
        orderMapper.saveOrUpdate(order);
        // TODO: 其他业务代码
        return true;
    }

    @Transition(key = Constants.RECEIVE_TRANSITION)
    @OnTransition(source = "WAIT_RECEIVE", target = "FINISH")
    public boolean receiveTransition(Message<OrderStateChangeEvent> message) {
        Order order = (Order) message.getHeaders().get("order");
        log.info("收货，状态机反馈信息：{}", message.getHeaders());
        // 更新订单
        assert order != null;
        order.setStatus(OrderState.FINISH);
        // 持久化到数据库
        orderMapper.saveOrUpdate(order);
        // TODO: 其他业务代码
        return true;
    }

}