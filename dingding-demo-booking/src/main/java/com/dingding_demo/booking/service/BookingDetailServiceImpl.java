package com.dingding_demo.booking.service;

import com.dingding.model.MarkAsRpc;
import com.dingding_demo.common.booking.BookingDetailService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingDetailServiceImpl implements BookingDetailService {

    @MarkAsRpc
    @Override
    public Object getBookingByUserId(int userId) {
        return "Hello I am booking service";
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
