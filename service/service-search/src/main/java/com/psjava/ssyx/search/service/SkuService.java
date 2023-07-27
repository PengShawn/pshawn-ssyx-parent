package com.psjava.ssyx.search.service;

import com.psjava.ssyx.model.search.SkuEs;
import com.psjava.ssyx.vo.search.SkuEsQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SkuService {
    //上架商品
    void upperSku(Long skuId);

    //下架商品
    void lowerSku(Long skuId);

    //获取爆品商品
    List<SkuEs> findHotSkuList();

    //查询分类商品
    Page<SkuEs> search(Pageable pageable, SkuEsQueryVo searchParamVo);

    //更新商品incrHotScore
    void incrHotScore(Long skuId);
}
