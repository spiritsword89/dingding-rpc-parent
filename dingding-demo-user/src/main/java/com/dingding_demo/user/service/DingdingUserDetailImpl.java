package com.dingding_demo.user.service;

import com.dingding_demo.common.booking.BookingDetailService;
import com.dingding_demo.common.user.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DingdingUserDetailImpl implements UserDetailService {

    @Autowired
    private BookingDetailService bookingDetailService;

    @Override
    public void checkUser() {
        bookingDetailService.getBookingByUserId(1);
    }
}
