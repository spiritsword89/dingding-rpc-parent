package com.dingding_demo.user.rpc;

import com.dingding.config.AutoRemoteInjection;
import com.dingding_demo.common.booking.BookingDetailService;
import com.dingding_demo.user.service.BookingServiceFallBack;

@AutoRemoteInjection(requestClientId = "demo-booking", fallbackClass = BookingServiceFallBack.class)
public interface MyUserBookingRpcService extends BookingDetailService {
}
