package com.psjava.ssyx.cart.service.impl;

import com.psjava.ssyx.cart.service.CartInfoService;
import com.psjava.ssyx.client.product.ProductFeignClient;
import com.psjava.ssyx.common.constant.RedisConst;
import com.psjava.ssyx.common.exception.SsyxException;
import com.psjava.ssyx.common.result.ResultCodeEnum;
import com.psjava.ssyx.enums.SkuType;
import com.psjava.ssyx.model.order.CartInfo;
import com.psjava.ssyx.model.product.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartInfoServiceImpl implements CartInfoService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ProductFeignClient productFeignClient;

    //通过skuid删除购物车
    @Override
    public void deleteCart(Long skuId, Long userId) {
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(getCartKey(userId));
        if(Boolean.TRUE.equals(boundHashOps.hasKey(skuId.toString()))) {
            boundHashOps.delete(skuId.toString());
        }
    }

    //清空购物车
    @Override
    public void deleteAllCart(Long userId) {
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        Objects.requireNonNull(hashOperations.values()).forEach(cartInfo -> {
            hashOperations.delete(cartInfo.getSkuId().toString());
        });
    }

    //批量删除购物车
    @Override
    public void batchDeleteCart(List<Long> skuIdList, Long userId) {
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId -> {
            hashOperations.delete(skuId.toString());
        });
    }

    //查询购物车列表
    @Override
    public List<CartInfo> getCartList(Long userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (StringUtils.isEmpty(userId)) return cartInfoList;

        // 定义key user:userId:cart
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        // 获取数据
        cartInfoList = hashOperations.values();
        if (!CollectionUtils.isEmpty(cartInfoList)) {
            // 购物车列表显示有顺序：按照商品的更新时间 降序
            cartInfoList.sort((o1, o2) -> {
                // str1 = ab str2 = ac;
                return o2.getCreateTime().compareTo(o1.getCreateTime());
            });
        }
        return cartInfoList;
    }


    @Override
    public void addToCart(Long skuId, Long userId, Integer skuNum) {
        // 定义key user:userId:cart
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = null;
        // 如果购物车中有该商品并且没有失效，则更新个数并选中 && cartInfo.getStatus().intValue() == 1
        if (hashOperations.hasKey(skuId.toString())) {
            cartInfo = hashOperations.get(skuId.toString());
            int currentSkuNum = cartInfo.getSkuNum() + skuNum;
            if(currentSkuNum < 1) {
                return;
            }

            //获取用户当前已经购买的sku个数，sku限量，每天不能超买
            //添加购物车数量
            cartInfo.setSkuNum(currentSkuNum);
            //当天购买数量
            cartInfo.setCurrentBuyNum(currentSkuNum);
            //大于限购个数，不能更新个数
            if(currentSkuNum >= cartInfo.getPerLimit()) {
                throw new SsyxException(ResultCodeEnum.SKU_LIMIT_ERROR);
            }
            cartInfo.setIsChecked(1);
            cartInfo.setUpdateTime(new Date());
        } else {
            //第一次添加只能添加一个
            skuNum = 1;

            // 当购物车中没用该商品的时候，则直接添加到购物车！insert
            cartInfo = new CartInfo();
            // 购物车数据是从商品详情得到 {skuInfo}
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            if(null == skuInfo) {
                throw new SsyxException(ResultCodeEnum.DATA_ERROR);
            }
            cartInfo.setSkuId(skuId);
            cartInfo.setCategoryId(skuInfo.getCategoryId());
            cartInfo.setSkuType(skuInfo.getSkuType());
            cartInfo.setIsNewPerson(skuInfo.getIsNewPerson());
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setCurrentBuyNum(skuNum);
            cartInfo.setSkuType(SkuType.COMMON.getCode());
            cartInfo.setPerLimit(skuInfo.getPerLimit());
            cartInfo.setImgUrl(skuInfo.getImgUrl());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setWareId(skuInfo.getWareId());
            cartInfo.setIsChecked(1);
            cartInfo.setStatus(1);
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
        }

        // 更新缓存
        hashOperations.put(skuId.toString(), cartInfo);
        // 设置过期时间
        this.setCartKeyExpire(cartKey);
    }

    //返回购物车在redis中的key
    private String getCartKey(Long userId) {
        //定义key user:userId:cart
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

    //设置key的过期时间！
    private void setCartKeyExpire(String cartKey) {
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    @Override
    public void checkCart(Long userId, Integer isChecked, Long skuId) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = boundHashOps.get(skuId.toString());
        if(null != cartInfo) {
            cartInfo.setIsChecked(isChecked);
            boundHashOps.put(skuId.toString(), cartInfo);
            this.setCartKeyExpire(cartKey);
        }
    }

    @Override
    public void checkAllCart(Long userId, Integer isChecked) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(cartKey);
        Objects.requireNonNull(boundHashOps.values()).forEach(cartInfo -> {
            cartInfo.setIsChecked(isChecked);
            boundHashOps.put(cartInfo.getSkuId().toString(), cartInfo);
        });
        this.setCartKeyExpire(cartKey);
    }

    @Override
    public void batchCheckCart(List<Long> skuIdList, Long userId, Integer isChecked) {
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId -> {
            CartInfo cartInfo = hashOperations.get(skuId.toString());
            cartInfo.setIsChecked(isChecked);
            hashOperations.put(cartInfo.getSkuId().toString(), cartInfo);
        });
    }

    //根据用户Id 查询购物车列表
    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate.boundHashOps(this.getCartKey(userId));
        return Objects.requireNonNull(boundHashOps.values()).stream().filter((cartInfo) -> cartInfo.getIsChecked() == 1).collect(Collectors.toList());
    }

    //根据userId删除选中购物车记录
    @Override
    public void deleteCartChecked(Long userId) {
        List<CartInfo> cartInfoList = this.getCartCheckedList(userId);
        List<Long> skuIdList = cartInfoList.stream().map(CartInfo::getSkuId).collect(Collectors.toList());

        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId -> {
            hashOperations.delete(skuId.toString());
        });
    }
}
