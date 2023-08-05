package com.psjava.ssyx.payment.service;

import java.util.Map;

public interface WeixinService {
    //根据订单号下单，生成支付链接
    Map<String, String> createJsapi(String orderNo);

    //根据订单号去微信第三方查询支付状态
    Map<String, String> queryPayStatus(String orderNo, String paymentType);
}
