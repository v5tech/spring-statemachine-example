package net.ameizi.spring.statemachine.config;

import lombok.extern.slf4j.Slf4j;
import net.ameizi.spring.statemachine.enums.OrderStateChangeEvent;
import net.ameizi.spring.statemachine.enums.OrderState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

/**
 * 订单状态机配置类
 */
@Slf4j
@Configuration
@EnableStateMachine(name = "orderStateMachine")
public class OrderStateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderState, OrderStateChangeEvent> {

    /**
     * 配置监听器
     *
     * @param config the {@link StateMachineConfigurationConfigurer}
     * @throws Exception
     */
    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderState, OrderStateChangeEvent> config) throws Exception {
        config.withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    /**
     * 配置状态
     *
     * @param states the {@link StateMachineStateConfigurer}
     * @throws Exception
     */
    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderStateChangeEvent> states) throws Exception {
        states.withStates()
                // 初始状态
                .initial(OrderState.WAIT_PAYMENT)
                // 全部状态
                .states(EnumSet.allOf(OrderState.class));
    }

    /**
     * 配置状态转换事件关系
     *
     * @param transitions the {@link StateMachineTransitionConfigurer}
     * @throws Exception
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderStateChangeEvent> transitions) throws Exception {
        transitions.withExternal()
                // 支付事件：待支付 -> 待发货
                .source(OrderState.WAIT_PAYMENT).target(OrderState.WAIT_DELIVER).event(OrderStateChangeEvent.PAYED)
                // 发货事件：待发货 -> 待收货
                .and().withExternal().source(OrderState.WAIT_DELIVER).target(OrderState.WAIT_RECEIVE).event(OrderStateChangeEvent.DELIVERY)
                // 收货事件：待收货 -> 已完成
                .and().withExternal().source(OrderState.WAIT_RECEIVE).target(OrderState.FINISH).event(OrderStateChangeEvent.RECEIVED);
    }

    /**
     * 状态机监听器
     *
     * @return
     */
    @Bean
    public StateMachineListener<OrderState, OrderStateChangeEvent> listener() {
        return new StateMachineListenerAdapter<OrderState, OrderStateChangeEvent>() {
            @Override
            public void stateChanged(State<OrderState, OrderStateChangeEvent> from, State<OrderState, OrderStateChangeEvent> to) {
                log.info("订单状态发生变更，from：{}，to：{}", from != null ? from.getId() : null, to != null ? to.getId() : null);
            }
        };
    }

}