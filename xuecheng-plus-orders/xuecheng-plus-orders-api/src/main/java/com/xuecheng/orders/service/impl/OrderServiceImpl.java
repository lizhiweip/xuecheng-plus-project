package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.domain.PosOrderDeviceInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author lzw
 * @Date 2025/1/20 12:53
 * @description  订单相关接口
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private XcOrdersMapper ordersMapper;
    @Autowired
    private XcOrdersGoodsMapper goodsMapper;
    @Autowired
    private XcPayRecordMapper payRecordMapper;
    @Value("${pay.qrcodeurl}")
    String qrcodeUrl;

    @Transactional
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {

        //插入订单表，订单主表，订单明细表
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);

        //插入支付记录
        XcPayRecord payRecord = createPayRecord(xcOrders);
        Long payNo = payRecord.getPayNo();

        //生成二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        //支付二维码的url
        String url = String.format(qrcodeUrl, payNo);
        //二维码图片
        String qrCode = null;
        try{
           qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        }catch (Exception e){
            XueChengPlusException.cast("生成二维码出错");
        }

        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }

    //保存订单信息
    @Transactional
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto){

        //插入订单表，订单主表，订单明细表
        //进行幂等性判断，同一个选课记录只能有一个订单
        XcOrders xcOrders = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (xcOrders != null){
            return xcOrders;
        }

        //插入订单主表
        xcOrders = new XcOrders();
        //使用雪花算法生成订单号
        xcOrders.setId(IdWorkerUtils.getInstance().nextId());
        xcOrders.setTotalPrice(addOrderDto.getTotalPrice());
        xcOrders.setCreateDate(LocalDateTime.now());
        xcOrders.setStatus("600001");//未支付
        xcOrders.setUserId(userId);
        xcOrders.setOrderType("60201");
        xcOrders.setOrderName(addOrderDto.getOrderName());
        xcOrders.setOrderDescrip(addOrderDto.getOrderDescrip());
        xcOrders.setOrderDetail(addOrderDto.getOrderDetail());
        xcOrders.setOutBusinessId(addOrderDto.getOutBusinessId());//如果是选课这里记录选课表的id
        int insert = ordersMapper.insert(xcOrders);
        if(insert <= 0){
            XueChengPlusException.cast("添加订单失败");
        }
        //订单id
        Long ordersId = xcOrders.getId();
        //插入订单明细表
        //将前端传入的明细的json串转成list
        String orderDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        //遍历xcOrdersGoods插入订单明细表
        xcOrdersGoods.forEach(goods -> {
            goods.setOrderId(ordersId);
            //插入订单明细
            int insert1 = goodsMapper.insert(goods);

        });
       return xcOrders;
    }


    //保存支付记录
    public XcPayRecord createPayRecord(XcOrders orders){
        //订单id
        Long ordersId = orders.getId();
        XcOrders xcOrders = ordersMapper.selectById(ordersId);
        //如果此订单不存在不再添加支付记录
        if(xcOrders == null){
            XueChengPlusException.cast("订单不存在");
        }
        //订单状态
        String status = xcOrders.getStatus();
        if("601002".equals(status)){
            //如果此订单支付结果为成功，不再添加支付记录，避免重复支付
            XueChengPlusException.cast("此订单已支付");
        }
        XcPayRecord xcPayRecord = new XcPayRecord();
        xcPayRecord.setPayNo(IdWorkerUtils.getInstance().nextId());//支付记录号，将来传给支付宝
        xcPayRecord.setOrderId(ordersId);
        xcPayRecord.setOrderName(xcOrders.getOrderName());
        xcPayRecord.setTotalPrice(xcOrders.getTotalPrice());
        xcPayRecord.setCurrency("CNY");
        xcPayRecord.setStatus(status);//未支付
        xcPayRecord.setUserId(xcOrders.getUserId());
        int insert = payRecordMapper.insert(xcPayRecord);
        if(insert <= 0){
            XueChengPlusException.cast("插入支付记录失败");
        }
       return xcPayRecord;

    }



    /*
    根据业务id查询订单，业务id是选课记录表中的主键
     */
    public XcOrders getOrderByBusinessId(String businessId) {
        XcOrders orders = ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
        return orders;
    }

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        XcPayRecord xcPayRecord = payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
        return xcPayRecord;
    }


}
