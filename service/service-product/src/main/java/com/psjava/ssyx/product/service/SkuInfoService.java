package com.psjava.ssyx.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psjava.ssyx.model.product.SkuInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.psjava.ssyx.vo.product.SkuInfoQueryVo;
import com.psjava.ssyx.vo.product.SkuInfoVo;

/**
 * <p>
 * sku信息 服务类
 * </p>
 *
 * @author pshawn
 * @since 2023-06-28
 */
public interface SkuInfoService extends IService<SkuInfo> {

    //获取sku分页列表
    IPage<SkuInfo> selectPage(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo);

    //添加商品
    void saveSkuInfo(SkuInfoVo skuInfoVo);
}
