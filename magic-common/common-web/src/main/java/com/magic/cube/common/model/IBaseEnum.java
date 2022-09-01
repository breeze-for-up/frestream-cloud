package com.magic.cube.common.model;
/**
 * @description: 简单枚举类实现
 * 
 * @author: TJ
 * @date:  2022-05-19
 **/
public interface IBaseEnum<V> {

    V getValue();

    String getLabel();
}
