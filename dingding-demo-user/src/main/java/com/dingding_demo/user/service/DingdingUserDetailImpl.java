package com.dingding_demo.user.service;
import com.dingding.config.AutoRemoteInjection;
import com.dingding_demo.common.booking.BookingDetailService;
import com.dingding_demo.common.user.UserDetailService;
import com.dingding_demo.user.rpc.MyUserBookingRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DingdingUserDetailImpl implements UserDetailService {

    @Autowired
    private MyUserBookingRpcService bookingDetailService;

    // 代理对象： 内部是通过Netty的Channel发送请求到生产者进行方法调用，产生的结果原路返回

    @Override
    public String checkUser() {
        // token
        System.out.println("Hello");
        return bookingDetailService.getBookingByUserId(1).toString();
    }
}
