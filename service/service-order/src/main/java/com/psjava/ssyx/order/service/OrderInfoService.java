package com.psjava.ssyx.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.psjava.ssyx.model.order.OrderInfo;
import com.psjava.ssyx.vo.order.OrderConfirmVo;
import com.psjava.ssyx.vo.order.OrderSubmitVo;

/**
 * <p>
 * 订单 服务类
 * </p>
 *
 * @author pshawn
 * @since 2023-08-01
 */
public interface OrderInfoService extends IService<OrderInfo> {

    //确认订单
    OrderConfirmVo confirmOrder();

    //生成订单
    Long submitOrder(OrderSubmitVo orderParamVo);

    //订单详情
    OrderInfo getOrderInfoById(Long orderId);

    //根据orderNo查询订单信息
    OrderInfo getOrderInfoByOrderNo(String orderNo);

    //支付成功！ 修改订单状态为已支付
    void orderPay(String orderNo);
}
