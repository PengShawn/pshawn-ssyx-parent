package com.psjava.ssyx.order.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psjava.ssyx.common.auth.AuthContextHolder;
import com.psjava.ssyx.common.result.Result;
import com.psjava.ssyx.model.order.OrderInfo;
import com.psjava.ssyx.order.service.OrderInfoService;
import com.psjava.ssyx.order.service.OrderItemService;
import com.psjava.ssyx.vo.order.OrderConfirmVo;
import com.psjava.ssyx.vo.order.OrderSubmitVo;
import com.psjava.ssyx.vo.order.OrderUserQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

    @ApiOperation("根据orderNo查询订单信息")
    @GetMapping("inner/getOrderInfo/{orderNo}")
    public OrderInfo getOrderInfoByOrderNo(@PathVariable("orderNo") String orderNo) {
        return orderInfoService.getOrderInfoByOrderNo(orderNo);
    }

    @ApiOperation(value = "获取用户订单分页列表")
    @GetMapping("auth/findUserOrderPage/{page}/{limit}")
    public Result findUserOrderPage(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,

            @ApiParam(name = "orderVo", value = "查询对象", required = false)
            OrderUserQueryVo orderUserQueryVo) {
        Long userId = AuthContextHolder.getUserId();
        orderUserQueryVo.setUserId(userId);
        Page<OrderInfo> pageParam = new Page<>(page, limit);
        IPage<OrderInfo> pageModel = orderInfoService.findUserOrderPage(pageParam, orderUserQueryVo);
        return Result.ok(pageModel);
    }
}

