package com.psjava.ssyx.search.service;

public interface SkuService {
    //上架商品
    void upperSku(Long skuId);

    //下架商品
    void lowerSku(Long skuId);
}
