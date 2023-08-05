package com.psjava.ssyx.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psjava.ssyx.common.exception.SsyxException;
import com.psjava.ssyx.common.result.ResultCodeEnum;
import com.psjava.ssyx.enums.PaymentStatus;
import com.psjava.ssyx.enums.PaymentType;
import com.psjava.ssyx.model.order.OrderInfo;
import com.psjava.ssyx.model.order.PaymentInfo;
import com.psjava.ssyx.mq.constant.MqConst;
import com.psjava.ssyx.mq.service.RabbitService;
import com.psjava.ssyx.order.client.OrderFeignClient;
import com.psjava.ssyx.payment.mapper.PaymentInfoMapper;
import com.psjava.ssyx.payment.service.PaymentInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {
    @Autowired
    private OrderFeignClient orderFeignClient;
    @Autowired
    private RabbitService rabbitService;
    @Override
    public PaymentInfo getPaymentInfo(String orderNo, PaymentType paymentType) {
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOrderNo, orderNo);
        queryWrapper.eq(PaymentInfo::getPaymentType, paymentType);
        return baseMapper.selectOne(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PaymentInfo savePaymentInfo(String orderNo, PaymentType paymentType) {
        OrderInfo order = orderFeignClient.getOrderInfoByOrderNo(orderNo);
        if(null == order) {
            throw new SsyxException(ResultCodeEnum.DATA_ERROR);
        }
        // 保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setUserId(order.getUserId());
        paymentInfo.setOrderNo(order.getOrderNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        String subject = "userID:" + order.getUserId() + "下订单";
        paymentInfo.setSubject(subject);
        //paymentInfo.setTotalAmount(order.getTotalAmount());
        //TODO 为了测试
        paymentInfo.setTotalAmount(new BigDecimal("0.01"));

        baseMapper.insert(paymentInfo);
        return paymentInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void paySuccess(String orderNo, PaymentType paymentType, Map<String,String> paramMap) {
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOrderNo, orderNo);
        queryWrapper.eq(PaymentInfo::getPaymentType, paymentType);
        PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);
        if (paymentInfo.getPaymentStatus() != PaymentStatus.UNPAID) {
            return;
        }

        PaymentInfo paymentInfoUpd = new PaymentInfo();
        paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
        String tradeNo = paymentType == PaymentType.WEIXIN ? paramMap.get("transaction_id") : paramMap.get("trade_no");
        paymentInfoUpd.setTradeNo(tradeNo);
        paymentInfoUpd.setCallbackTime(new Date());
        paymentInfoUpd.setCallbackContent(paramMap.toString());
        baseMapper.update(paymentInfoUpd, new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderNo, orderNo));
        // 表示交易成功！

        //发送消息
        rabbitService.sendMessage(MqConst.EXCHANGE_PAY_DIRECT, MqConst.ROUTING_PAY_SUCCESS, orderNo);
    }
}
