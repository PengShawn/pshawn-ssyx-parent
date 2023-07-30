package com.psjava.ssyx.cart.controller;

import com.psjava.ssyx.cart.service.CartInfoService;
import com.psjava.ssyx.client.activity.ActivityFeignClient;
import com.psjava.ssyx.common.auth.AuthContextHolder;
import com.psjava.ssyx.common.result.Result;
import com.psjava.ssyx.model.order.CartInfo;
import com.psjava.ssyx.vo.order.OrderConfirmVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "购物车接口")
@RestController
@RequestMapping("api/cart")
public class CartApiController {
    @Autowired
    private CartInfoService cartInfoService;
    @Autowired
    private ActivityFeignClient activityFeignClient;

    @ApiOperation("添加购物车")
    @GetMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("skuNum") Integer skuNum) {
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.addToCart(skuId, userId, skuNum);
        return Result.ok(null);
    }

    @ApiOperation(value="根据skuid删除购物车商品")
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId) {
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteCart(skuId, userId);
        return Result.ok(null);
    }

    @ApiOperation(value="清空购物车")
    @DeleteMapping("deleteAllCart")
    public Result deleteAllCart(){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteAllCart(userId);
        return Result.ok(null);
    }

    @ApiOperation(value="批量删除购物车")
    @PostMapping("batchDeleteCart")
    public Result batchDeleteCart(@RequestBody List<Long> skuIdList){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchDeleteCart(skuIdList, userId);
        return Result.ok(null);
    }

    @ApiOperation(value="查询购物车列表")
    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request) {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);
        return Result.ok(cartInfoList);
    }

    @ApiOperation(value="查询带优惠卷的购物车")
    @GetMapping("activityCartList")
    public Result activityCartList(HttpServletRequest request) {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);

        OrderConfirmVo orderTradeVo = activityFeignClient.findCartActivityAndCoupon(cartInfoList, userId);
        return Result.ok(orderTradeVo);
    }
}
