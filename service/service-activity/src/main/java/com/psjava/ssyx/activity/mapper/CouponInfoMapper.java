package com.psjava.ssyx.activity.mapper;

import com.psjava.ssyx.model.activity.CouponInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 优惠券信息 Mapper 接口
 * </p>
 *
 * @author pshawn
 * @since 2023-06-30
 */
@Repository
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {
    //根据skuid和userid查询优惠券信息
    List<CouponInfo> selectCouponInfoList(@Param("skuId") Long skuId, @Param("categoryId") Long categoryId, @Param("userId") Long userId);

    //获取用户全部优惠券
    List<CouponInfo> selectCartCouponInfoList(@Param("userId")Long userId);
}
