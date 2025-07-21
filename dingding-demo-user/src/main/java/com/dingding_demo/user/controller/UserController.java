package com.dingding_demo.user.controller;

import com.dingding_demo.common.user.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user/demo")
@RestController
public class UserController {

    @Autowired
    private UserDetailService userDetailService;

    @GetMapping("/result")
    public String getBookingDetails() {
        String result = userDetailService.checkUser();
        return result;
    }
}
