package com.psjava.ssyx.activity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psjava.ssyx.model.activity.ActivityInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.psjava.ssyx.model.activity.ActivityRule;
import com.psjava.ssyx.model.order.CartInfo;
import com.psjava.ssyx.model.product.SkuInfo;
import com.psjava.ssyx.vo.activity.ActivityRuleVo;
import com.psjava.ssyx.vo.order.CartInfoVo;
import com.psjava.ssyx.vo.order.OrderConfirmVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动表 服务类
 * </p>
 *
 * @author pshawn
 * @since 2023-06-30
 */
public interface ActivityInfoService extends IService<ActivityInfo> {

    //分页查询
    IPage<ActivityInfo> selectPage(Page<ActivityInfo> pageParam);

    //获取活动规则
    Map<String, Object> findActivityRuleList(Long activityId);

    //保存活动规则信息
    void saveActivityRule(ActivityRuleVo activityRuleVo);

    //根据关键字获取sku列表，活动使用
    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    //根据skuId获取促销规则信息
    List<ActivityRule> findActivityRuleBySkuId(Long skuId);

    //根据skuId列表获取促销信息
    Map<Long, List<String>> findActivity(List<Long> skuIdList);

    //根据skuId获取促销与优惠券信息
    Map<String, Object> findActivityAndCoupon(Long skuId, Long userId);

    //获取购物车满足条件的促销与优惠券信息
    OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId);

    //获取购物车对应规则数据，商品按规则分组
    List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList);
}
