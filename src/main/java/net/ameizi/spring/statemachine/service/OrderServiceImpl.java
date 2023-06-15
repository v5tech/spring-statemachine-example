package net.ameizi.spring.statemachine.service;

import lombok.extern.slf4j.Slf4j;
import net.ameizi.spring.statemachine.config.Constants;
import net.ameizi.spring.statemachine.entity.Order;
import net.ameizi.spring.statemachine.enums.OrderStateChangeEvent;
import net.ameizi.spring.statemachine.enums.OrderState;
import net.ameizi.spring.statemachine.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 订单服务
 * 待支付 -> 待发货 -> 待收货 -> 完结
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    /**
     * 状态机对象
     */
    @Resource
    private StateMachine<OrderState, OrderStateChangeEvent> orderStateMachine;

    /**
     * 状态机持久化,基于内存的
     */
    @Autowired
    private StateMachinePersister<OrderState, OrderStateChangeEvent, Order> stateMachineMemoryPersister;

    /**
     * 状态机持久化,基于Redis的
     */
    @Autowired
    private RedisStateMachinePersister<OrderState, OrderStateChangeEvent> stateMachineRedisPersister;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 创建订单
     *
     * @return
     */
    @Override
    public Order create(int id) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(OrderState.WAIT_PAYMENT);
        // 此处不需要发送OrderEvent事件，因为在状态机配置类中指定了初始状态为OrderState.WAIT_PAYMENT
        return orderMapper.saveOrUpdate(order);
    }

    /**
     * 支付订单
     *
     * @param id
     * @return
     */
    @Override
    public Order pay(int id) {
        Order order = orderMapper.select(id);
        log.info("线程名称：{}，尝试支付，订单号：{}", Thread.currentThread().getName(), id);
        if (!sendEvent(OrderStateChangeEvent.PAYED, order, Constants.PAY_TRANSITION)) {
            log.error("线程名称：{}，支付失败，状态异常，订单信息：{}", Thread.currentThread().getName(), order);
            throw new RuntimeException("支付失败，订单状态异常");
        }
        return order;
    }

    /**
     * 发货
     *
     * @param id
     * @return
     */
    @Override
    public Order deliver(int id) {
        Order order = orderMapper.select(id);
        log.info("线程名称：{}，尝试发货，订单号：{}", Thread.currentThread().getName(), id);
        if (!sendEvent(OrderStateChangeEvent.DELIVERY, order, Constants.DELIVER_TRANSITION)) {
            log.error("线程名称：{}，发货失败，状态异常，订单信息：{}", Thread.currentThread().getName(), order);
            throw new RuntimeException("发货失败，订单状态异常");
        }
        return order;
    }

    /**
     * 收货
     *
     * @param id
     * @return
     */
    @Override
    public Order receive(int id) {
        Order order = orderMapper.select(id);
        log.info("线程名称：{}，尝试收货，订单号：{}", Thread.currentThread().getName(), id);
        if (!sendEvent(OrderStateChangeEvent.RECEIVED, order, Constants.RECEIVE_TRANSITION)) {
            log.error("线程名称：{}，收货失败，状态异常，订单信息：{}", Thread.currentThread().getName(), order);
            throw new RuntimeException("收货失败，订单状态异常");
        }
        return order;
    }

    /**
     * 发送订单状态转换事件
     *
     * @param event      事件
     * @param order      订单
     * @param transition 过渡
     * @return 执行结果
     */
    private boolean sendEvent(OrderStateChangeEvent event, Order order, String transition) {
        synchronized (String.valueOf(order.getId()).intern()) {
            boolean sendEventResult;
            boolean result = false;
            try {
                // 启动状态机
                orderStateMachine.start();
                // 恢复状态机状态
                stateMachineRedisPersister.restore(orderStateMachine, String.valueOf(order.getId()));
                log.info("线程名称：{}，订单号：{}，状态机中的状态为：{}", Thread.currentThread().getName(), order.getId(), Objects.requireNonNull(orderStateMachine.getState()).getId());
                Message<OrderStateChangeEvent> eventMessage = MessageBuilder.withPayload(event).setHeader(Constants.ORDER_HEADER, order).build();
                sendEventResult = orderStateMachine.sendEvent(eventMessage);
                if (!sendEventResult) {
                    return false;
                }
                String key = transition + order.getId();
                // 取状态机的结果信息
                result = (Boolean) orderStateMachine.getExtendedState().getVariables().get(key);
                // 清除状态机的结果信息
                orderStateMachine.getExtendedState().getVariables().remove(key);
                // 若状态机执行业务代码成功，则持久化状态机
                if (result) {
                    // 持久化状态机
                    stateMachineRedisPersister.persist(orderStateMachine, String.valueOf(order.getId()));
                } else {
                    // 订单执行业务异常
                    return false;
                }
            } catch (Exception e) {
                log.error("订单操作失败：{}", e.getMessage());
            } finally {
                orderStateMachine.stop();
            }
            return result;
        }
    }
}