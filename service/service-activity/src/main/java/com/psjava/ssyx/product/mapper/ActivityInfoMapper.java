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

    List<Long> selectExistSkuIdList(@Param("skuIdList") List<Long> skuIdList);

    List<ActivityRule> selectActivityRuleList(@Param("skuId") Long skuId);
}
