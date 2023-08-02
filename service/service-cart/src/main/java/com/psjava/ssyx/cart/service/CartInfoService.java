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

    //更新选中状态
    void checkCart(Long userId, Integer isChecked, Long skuId);

    //选择所有购物车
    void checkAllCart(Long userId, Integer isChecked);

    //批量选择购物车
    void batchCheckCart(List<Long> skuIdList, Long userId, Integer isChecked);

    //根据用户Id 查询购物车列表
    List<CartInfo> getCartCheckedList(Long userId);

    //根据userId删除选中购物车记录
    void deleteCartChecked(Long userId);
}