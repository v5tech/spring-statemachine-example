package net.ameizi.spring.statemachine.config;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import net.ameizi.spring.statemachine.entity.Order;
import net.ameizi.spring.statemachine.enums.OrderStateChangeEvent;
import net.ameizi.spring.statemachine.enums.OrderState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.RepositoryStateMachinePersist;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.redis.RedisStateMachineContextRepository;
import org.springframework.statemachine.redis.RedisStateMachinePersister;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Configuration
public class OrderStateMachinePersisterConfig {

    private final Map<Order, StateMachineContext<OrderState, OrderStateChangeEvent>> map = new HashMap<>();

    /**
     * 状态机持久化-基于内存实现
     *
     * @return
     */
    @Bean(name = "stateMachineMemoryPersister")
    public StateMachinePersister<OrderState, OrderStateChangeEvent, Order> stateMachineMemoryPersister() {
        return new DefaultStateMachinePersister<>(new StateMachinePersist<OrderState, OrderStateChangeEvent, Order>() {
            @Override
            public void write(StateMachineContext<OrderState, OrderStateChangeEvent> context, Order contextObj) {
                log.info("持久化状态机：context：{}，contextObj：{}", JSONUtil.toJsonStr(context), JSONUtil.toJsonStr(contextObj));
                map.put(contextObj, context);
            }

            @Override
            public StateMachineContext<OrderState, OrderStateChangeEvent> read(Order contextObj) {
                log.info("获取contextObj：{}", JSONUtil.toJsonStr(contextObj));
                StateMachineContext<OrderState, OrderStateChangeEvent> stateMachineContext = map.get(contextObj);
                log.info("获取状态机stateMachineContext：{}", JSONUtil.toJsonStr(stateMachineContext));
                return stateMachineContext;
            }
        });

    }

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * 状态机持久化-基于Redis实现
     *
     * @return
     */
    @Bean(name = "stateMachineRedisPersister")
    public RedisStateMachinePersister<OrderState, OrderStateChangeEvent> stateMachineRedisPersister() {
        RedisStateMachineContextRepository<OrderState, OrderStateChangeEvent> repository = new RedisStateMachineContextRepository<>(redisConnectionFactory);
        return new RedisStateMachinePersister<>(new RepositoryStateMachinePersist<>(repository));
    }

}
