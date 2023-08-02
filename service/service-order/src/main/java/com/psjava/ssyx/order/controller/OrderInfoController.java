package com.psjava.ssyx.order.controller;


import com.psjava.ssyx.common.result.Result;
import com.psjava.ssyx.model.order.OrderInfo;
import com.psjava.ssyx.order.service.OrderInfoService;
import com.psjava.ssyx.vo.order.OrderConfirmVo;
import com.psjava.ssyx.vo.order.OrderSubmitVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 订单 前端控制器
 * </p>
 *
 * @author pshawn
 * @since 2023-08-01
 */
@Api(value = "Order管理", tags = "Order管理")
@RestController
@RequestMapping("/api/order")
public class OrderInfoController {
    @Resource
    private OrderInfoService orderInfoService;

    @ApiOperation("确认订单")
    @GetMapping("auth/confirmOrder")
    public Result confirm() {
        OrderConfirmVo orderConfirmVo = orderInfoService.confirmOrder();
        return Result.ok(orderConfirmVo);
    }

    @ApiOperation("生成订单")
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderSubmitVo orderParamVo) {
        Long orderId = orderInfoService.submitOrder(orderParamVo);
        return Result.ok(orderId);
    }

    @ApiOperation("获取订单详情")
    @GetMapping("auth/getOrderInfoById/{orderId}")
    public Result getOrderInfoById(@PathVariable("orderId") Long orderId){
        OrderInfo orderInfo = orderInfoService.getOrderInfoById(orderId);
        return Result.ok(orderInfo);
    }
}

