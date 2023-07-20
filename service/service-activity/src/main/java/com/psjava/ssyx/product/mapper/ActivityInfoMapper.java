package com.psjava.ssyx.product.mapper;

import com.psjava.ssyx.model.activity.ActivityInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psjava.ssyx.model.activity.ActivityRule;
import feign.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 活动表 Mapper 接口
 * </p>
 *
 * @author pshawn
 * @since 2023-06-30
 */
@Repository
public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {
    //如果之前参加过，活动正在进行中，排除商品
    List<Long> selectExistSkuIdList(@Param("skuIdList") List<Long> skuIdList);

    //根据skuId进行查询，查询sku对应活动里面规则列表
    List<ActivityRule> selectActivityRuleList(@Param("skuId") Long skuId);

    //根据skuId进行查询，查询sku对应活动里面规则列表
    List<ActivityRule> findActivityRule(@Param("skuId") Long skuId);
}
