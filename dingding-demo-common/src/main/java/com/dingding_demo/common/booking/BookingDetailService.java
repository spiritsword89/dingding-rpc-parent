package com.dingding_demo.common.booking;

import com.dingding.client.RemoteService;

import java.util.List;

public interface BookingDetailService extends RemoteService {
    public Object getBookingByUserId(int userId);
    //getBookingByUserId.2.Integer.String
    public Object getBookingByUserId(int userId, String userName);
    public Object getBookingByUserId(String username);
    public List<Object> getAllBookings();

    //100+ methods
}
