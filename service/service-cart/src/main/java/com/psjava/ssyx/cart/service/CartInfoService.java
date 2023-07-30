package com.psjava.ssyx.cart.service;

import com.psjava.ssyx.model.order.CartInfo;

import java.util.List;

public interface CartInfoService {

    //添加购物车 用户Id，商品Id，商品数量。
    void addToCart(Long skuId, Long userId, Integer skuNum);

    //根据skuid删除购物车
    void deleteCart(Long skuId, Long userId);

    //清空购物车
    void deleteAllCart(Long userId);

    //批量删除购物车
    void batchDeleteCart(List<Long> skuIdList, Long userId);

    //查询购物车列表
    List<CartInfo> getCartList(Long userId);
}