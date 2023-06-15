package net.ameizi.spring.statemachine.aop;

import lombok.extern.slf4j.Slf4j;
import net.ameizi.spring.statemachine.config.Constants;
import net.ameizi.spring.statemachine.entity.Order;
import net.ameizi.spring.statemachine.enums.OrderState;
import net.ameizi.spring.statemachine.enums.OrderStateChangeEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class TransitionAspect {

    // 拦截Transition注解
    @Pointcut("@annotation(net.ameizi.spring.statemachine.aop.Transition)")
    private void transitionPointCut() {
    }

    @Resource
    private StateMachine<OrderState, OrderStateChangeEvent> orderStateMachine;

    @Around("transitionPointCut()")
    public Object logResultAround(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        log.info("参数args：{}", args);
        Message message = (Message) args[0];
        Order order = (Order) message.getHeaders().get(Constants.ORDER_HEADER);
        Assert.notNull(order, "订单信息有误！");
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        // 获取Transition注解
        Transition transition = method.getAnnotation(Transition.class);
        String key = transition.key() + order.getId();
        Object returnVal = false;
        try {
            // 执行方法
            returnVal = pjp.proceed();
            // 如果业务执行正常，执行结果赋为true
            orderStateMachine.getExtendedState().getVariables().put(key, true);
        } catch (Throwable e) {
            log.error("捕获到业务执行异常：{}", e.getMessage());
            // 如果业务执行异常，执行结果赋为false
            orderStateMachine.getExtendedState().getVariables().put(key, false);
        }
        return returnVal;
    }

}
