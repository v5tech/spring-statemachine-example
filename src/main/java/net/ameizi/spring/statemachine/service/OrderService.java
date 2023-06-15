package net.ameizi.spring.statemachine.service;

import net.ameizi.spring.statemachine.entity.Order;
/**
 * 订单服务
 *
 */
public interface OrderService {

    /**
     * 创建
     * 
     * @return
     */
    Order create(int id);

    /**
     * 支付
     * 
     * @param id
     * @return
     */
    Order pay(int id);

    /**
     * 发货
     * 
     * @param id
     * @return
     */
    Order deliver(int id);

    /**
     * 收货
     * 
     * @param id
     * @return
     */
    Order receive(int id);

}