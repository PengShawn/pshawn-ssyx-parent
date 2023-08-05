package com.psjava.ssyx.product.mapper;

import com.psjava.ssyx.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * sku信息 Mapper 接口
 * </p>
 *
 * @author pshawn
 * @since 2023-06-28
 */
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    SkuInfo checkStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    Integer lockStock(@Param("skuId")Long skuId, @Param("skuNum")Integer skuNum);

    Integer unlockStock(@Param("skuId")Long skuId, @Param("skuNum")Integer skuNum);

    Integer minusStock(@Param("skuId")Long skuId, @Param("skuNum")Integer skuNum);
}
