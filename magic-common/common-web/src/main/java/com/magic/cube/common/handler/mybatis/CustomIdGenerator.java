package com.magic.cube.common.handler.mybatis;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.magic.cube.core.utils.IdUtil;
import org.springframework.stereotype.Component;

/**
 * @description: 自定义生成 15 位雪花ID, 兼容js中 number 类型精度
 * 
 * @author: TJ
 * @date:  2022-05-13
 **/
@Component
public class CustomIdGenerator implements IdentifierGenerator {

    @Override
    public Number nextId(Object entity) {
        return IdUtil.nextId();
    }
}
