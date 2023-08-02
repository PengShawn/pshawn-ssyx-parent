package com.psjava.ssyx.activity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psjava.ssyx.model.activity.CouponInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.psjava.ssyx.model.order.CartInfo;
import com.psjava.ssyx.vo.activity.CouponRuleVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 优惠券信息 服务类
 * </p>
 *
 * @author pshawn
 * @since 2023-06-30
 */
public interface CouponInfoService extends IService<CouponInfo> {
    //优惠卷分页查询
    IPage<CouponInfo> selectPage(Page<CouponInfo> pageParam);

    //根据id获取优惠券
    CouponInfo getCouponInfo(String id);

    //根据优惠卷id获取优惠券规则列表
    Map<String, Object> findCouponRuleList(Long couponId);

    //新增优惠券规则
    void saveCouponRule(CouponRuleVo couponRuleVo);

    //根据关键字获取sku列表，活动使用
    List<CouponInfo> findCouponByKeyword(String keyword);

    //根据skuid和userid查询优惠券信息
    List<CouponInfo> findCouponInfoList(Long skuId, Long userId);

    //获取购物车中可使用优惠券列表
    List<CouponInfo> findCartCouponInfo(List<CartInfo> cartInfoList, Long userId);

    //获取优惠券范围对应的购物车列表
    CouponInfo findRangeSkuIdList(List<CartInfo> cartInfoList, Long couponId);

    //更新优惠卷使用状态
    void updateCouponInfoUseStatus(Long couponId, Long userId, Long orderId);
}
