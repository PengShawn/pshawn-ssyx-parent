package com.psjava.ssyx.payment.service;

import com.psjava.ssyx.enums.PaymentType;
import com.psjava.ssyx.model.order.PaymentInfo;

import java.util.Map;

public interface PaymentInfoService {
    //保存交易记录
    PaymentInfo savePaymentInfo(String orderNo, PaymentType paymentType);

    PaymentInfo getPaymentInfo(String orderNo, PaymentType paymentType);

    //支付成功
    void paySuccess(String orderNo,PaymentType paymentType, Map<String,String> paramMap);
}
