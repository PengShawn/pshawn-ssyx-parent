package com.psjava.ssyx.search.service;

import com.psjava.ssyx.model.search.SkuEs;

import java.util.List;

public interface SkuService {
    //上架商品
    void upperSku(Long skuId);

    //下架商品
    void lowerSku(Long skuId);

    //获取爆品商品
    List<SkuEs> findHotSkuList();
}
