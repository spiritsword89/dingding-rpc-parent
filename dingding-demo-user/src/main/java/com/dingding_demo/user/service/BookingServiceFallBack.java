package com.dingding_demo.user.service;

import com.dingding_demo.common.booking.BookingDetailService;

import java.util.List;

public class BookingServiceFallBack implements BookingDetailService {

    @Override
    public Object getBookingByUserId(int userId) {
        return "Fallback When Proxy Fails";
    }

    @Override
    public Object getBookingByUserId(int userId, String userName) {
        return null;
    }

    @Override
    public Object getBookingByUserId(String username) {
        return null;
    }

    @Override
    public List<Object> getAllBookings() {
        return List.of();
    }
}
