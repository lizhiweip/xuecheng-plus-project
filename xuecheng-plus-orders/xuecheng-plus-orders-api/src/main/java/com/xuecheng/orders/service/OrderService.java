package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import io.swagger.models.auth.In;

/**
 * @Author lzw
 * @Date 2025/1/20 12:51
 * @description 订单相关service
 */
public interface OrderService {

    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);
    public XcPayRecord getPayRecordByPayno(String payNo);

}
