package net.ameizi.spring.statemachine.aop;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Transition {

    /**
     * 标记执行业务过渡key
     *
     * @return String
     */
    String key();

}