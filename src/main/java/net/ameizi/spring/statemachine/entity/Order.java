package net.ameizi.spring.statemachine.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.ameizi.spring.statemachine.enums.OrderState;

/**
 * 订单
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    /** 订单id */
    private Integer id;
    /** 状态 */
    private OrderState status;
}