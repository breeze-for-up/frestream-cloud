package com.magic.cube.system.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * 
 * @author: TJ
 * @date:  2022-08-31
 **/
@RestController
@RequestMapping("sys-user")
public class SysUserController {

    @GetMapping("getUser")
    public void getUser(String code) {
        System.out.println("=== getUser");

        IdWorker.initSequence(1L, 1L);
        System.out.println(IdWorker.getId());
    }

    public static void main(String[] args) {
        IdWorker.initSequence(1L, 1L);
        for (int i = 0; i < 5; i++) {
            System.out.println(IdWorker.getId());
        }
    }
}
