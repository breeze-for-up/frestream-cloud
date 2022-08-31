package com.magic.cube.common.annotations;

import java.lang.annotation.*;

/**
 * @description:
 *   用于从post请求的body中直接取参数绑定到接口入参, 类似于 @RequestParam
 *
 * @author: TJ
 * @date:  2022-05-19
 **/
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PostParam {

    /**
     * 要绑定的参数名
     */
    String value() default "";

    /**
     * not use
     */
    boolean required() default false;

    /**
     * 当 required=true 但没有获取到参数时的错误提示
     */
    String message() default "";

    /**
     * not use
     */
    String defaultValue() default "";
}
