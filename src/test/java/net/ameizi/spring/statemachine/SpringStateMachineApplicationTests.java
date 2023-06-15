package net.ameizi.spring.statemachine;

import net.ameizi.spring.statemachine.entity.Order;
import net.ameizi.spring.statemachine.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringStateMachineApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private OrderService orderService;

    @Test
    public void testOrder() {
        Thread.currentThread().setName("主线程");
        // Order order1 = orderService.create(1);
        // orderService.pay(order1.getId());
        // // 再次调用pay()方法会抛出java.lang.RuntimeException: 支付失败，订单状态异常
        // // orderService.pay(order1.getId());
        // new Thread("客户线程") {
        //     @Override
        //     public void run() {
        //         orderService.deliver(order1.getId());
        //         orderService.receive(order1.getId());
        //         assertSame(OrderState.FINISH, order1.getStatus());
        //     }
        // }.start();
        Order order2 = orderService.create(2);
        orderService.pay(order2.getId());
        orderService.deliver(order2.getId());
        orderService.receive(order2.getId());
    }

}
