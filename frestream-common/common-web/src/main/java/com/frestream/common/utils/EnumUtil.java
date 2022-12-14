package com.frestream.common.utils;

import com.frestream.common.model.IBaseEnum;

/**
 * @description: 枚举类基本方法
 * 
 * @author: TJ
 * @date:  2022-05-19
 **/
@SuppressWarnings("all")
public class EnumUtil {

    /**
     * 判断枚举中是否存在目标值
     * @param enums 目标枚举类
     * @param value 目标值
     * @return 是否存在-boolean
     */
    public static boolean isExist(Class<? extends IBaseEnum> enumclazz, Object value) {
        if (value == null) {
            return false;
        }
        for (IBaseEnum e : enumclazz.getEnumConstants()) {
            if (value.equals(e.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据枚举类中value获取label
     * @param enums 枚举类class
     * @param value 枚举中的value
     * @return label
     */
    public static String getLabelByValue(Class<? extends IBaseEnum> enumclazz, Object value) {
        if (value == null) {
            return "";
        }
        for (IBaseEnum e : enumclazz.getEnumConstants()) {
            if (value.toString().equals(e.getValue().toString())) {
                return e.getLabel().toString();
            }
        }
        return "";
    }

    /**
     * 根据label获取枚举value
     * @param enumclazz 枚举类class
     * @param label 枚举中的label
     * @return value
     */
    public static Object getValueByLabel(Class<? extends IBaseEnum> enumclazz, String label) {
        if (label == null) {
            return "";
        }
        for (IBaseEnum e : enumclazz.getEnumConstants()) {
            if (label.trim().equals(e.getLabel())) {
                return e.getValue();
            }
        }
        return "";
    }
}
