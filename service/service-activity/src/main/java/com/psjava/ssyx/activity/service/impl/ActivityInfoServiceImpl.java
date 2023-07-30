package com.psjava.ssyx.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psjava.ssyx.activity.mapper.ActivityInfoMapper;
import com.psjava.ssyx.activity.mapper.ActivityRuleMapper;
import com.psjava.ssyx.activity.mapper.ActivitySkuMapper;
import com.psjava.ssyx.activity.service.ActivityInfoService;
import com.psjava.ssyx.activity.service.CouponInfoService;
import com.psjava.ssyx.client.product.ProductFeignClient;
import com.psjava.ssyx.enums.ActivityType;
import com.psjava.ssyx.model.activity.ActivityInfo;
import com.psjava.ssyx.model.activity.ActivityRule;
import com.psjava.ssyx.model.activity.ActivitySku;
import com.psjava.ssyx.model.activity.CouponInfo;
import com.psjava.ssyx.model.order.CartInfo;
import com.psjava.ssyx.model.product.SkuInfo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psjava.ssyx.vo.activity.ActivityRuleVo;
import com.psjava.ssyx.vo.order.CartInfoVo;
import com.psjava.ssyx.vo.order.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 活动表 服务实现类
 * </p>
 *
 * @author pshawn
 * @since 2023-06-30
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {
    @Autowired
    private ActivityRuleMapper activityRuleMapper;
    @Autowired
    private ActivitySkuMapper activitySkuMapper;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private CouponInfoService couponInfoService;

    ////分页查询
    @Override
    public IPage<ActivityInfo> selectPage(Page<ActivityInfo> pageParam) {
        IPage<ActivityInfo> page = baseMapper.selectPage(pageParam, null);
        page.getRecords().forEach(item -> {
            item.setActivityTypeString(item.getActivityType().getComment());
        });
        return page;
    }

    //获取活动规则
    @Override
    public Map<String, Object> findActivityRuleList(Long activityId) {
        Map<String, Object> result = new HashMap<>();
        //根据活动id查询所有规则列表
        LambdaQueryWrapper<ActivityRule> activityRuleWrapper = new LambdaQueryWrapper<>();
        activityRuleWrapper.eq(ActivityRule::getActivityId, activityId);
        List<ActivityRule> activityRuleList = activityRuleMapper.selectList(activityRuleWrapper);
        result.put("activityRuleList", activityRuleList);
        //根据活动id查询参与活动的商品skuid列表
        LambdaQueryWrapper<ActivitySku> activitySkuWrapper = new LambdaQueryWrapper<>();
        activitySkuWrapper.eq(ActivitySku::getActivityId, activityId);
        List<ActivitySku> activitySkuList = activitySkuMapper.selectList(activitySkuWrapper);
        List<Long> skuIdList = activitySkuList.stream().map(ActivitySku::getSkuId).collect(Collectors.toList());
        //根据sku idList获取sku商品信息，远程调用
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(skuIdList);
        result.put("skuInfoList", skuInfoList);
        return result;
    }

    //保存活动规则信息
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveActivityRule(ActivityRuleVo activityRuleVo) {
        activityRuleMapper.delete(new LambdaQueryWrapper<ActivityRule>().eq(ActivityRule::getActivityId, activityRuleVo.getActivityId()));
        activitySkuMapper.delete(new LambdaQueryWrapper<ActivitySku>().eq(ActivitySku::getActivityId, activityRuleVo.getActivityId()));

        List<ActivityRule> activityRuleList = activityRuleVo.getActivityRuleList();
        List<ActivitySku> activitySkuList = activityRuleVo.getActivitySkuList();

        ActivityInfo activityInfo = baseMapper.selectById(activityRuleVo.getActivityId());
        for(ActivityRule activityRule : activityRuleList) {
            activityRule.setActivityId(activityRuleVo.getActivityId());
            activityRule.setActivityType(activityInfo.getActivityType());
            activityRuleMapper.insert(activityRule);
        }

        for(ActivitySku activitySku : activitySkuList) {
            activitySku.setActivityId(activityRuleVo.getActivityId());
            activitySkuMapper.insert(activitySku);
        }
    }

    ////根据关键字获取sku列表，活动使用
    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoByKeyword(keyword);
        if(skuInfoList.size() == 0) return skuInfoList;
        List<Long> skuIdList = skuInfoList.stream().map(SkuInfo::getId).collect(Collectors.toList());

        List<SkuInfo> notExistSkuInfoList = new ArrayList<>();
        //已经存在的skuId，一个sku只能参加一个促销活动，所以存在的得排除
        //TODO 这里的selectExistSkuIdList有问题，目前是用当前时间进行判断
        List<Long> existSkuIdList = baseMapper.selectExistSkuIdList(skuIdList);
        for(SkuInfo skuInfo : skuInfoList) {
            if(!existSkuIdList.contains(skuInfo.getId())) {
                notExistSkuInfoList.add(skuInfo);
            }
        }
        return notExistSkuInfoList;
    }

    //根据skuId获取促销规则信息
    @Override
    public List<ActivityRule> findActivityRuleBySkuId(Long skuId) {
        List<ActivityRule> activityRuleList = baseMapper.selectActivityRuleList(skuId);
        if(!CollectionUtils.isEmpty(activityRuleList)) {
            for(ActivityRule activityRule : activityRuleList) {
                activityRule.setRuleDesc(this.getRuleDesc(activityRule));
            }
        }
        return activityRuleList;
    }

    //根据skuId列表获取促销信息
    @Override
    public Map<Long, List<String>> findActivity(List<Long> skuIdList) {
        Map<Long, List<String>> result = new HashMap<>();
        //skuIdList遍历，得到每个skuId
        skuIdList.forEach(skuId -> {
            //根据skuId进行查询，查询sku对应活动里面规则列表
            List<ActivityRule> activityRuleList =
                    baseMapper.findActivityRule(skuId);
            //数据封装，规则名称
            if(!CollectionUtils.isEmpty(activityRuleList)) {
                List<String> ruleList = new ArrayList<>();
                //把规则名称处理
                for (ActivityRule activityRule:activityRuleList) {
                    ruleList.add(this.getRuleDesc(activityRule));
                }
                result.put(skuId,ruleList);
            }
        });
        return result;
    }

    //根据skuId获取促销与优惠券信息
    @Override
    public Map<String, Object> findActivityAndCoupon(Long skuId, Long userId) {
        //一个sku只能有一个促销活动，一个活动有多个活动规则（如满赠，满100送10，满500送50）
        List<ActivityRule> activityRuleList = this.findActivityRuleBySkuId(skuId);

        //获取优惠券信息
        List<CouponInfo> couponInfoList = couponInfoService.findCouponInfoList(skuId, userId);

        Map<String, Object> map = new HashMap<>();
        map.put("activityRuleList", activityRuleList);
        map.put("couponInfoList", couponInfoList);
        return map;
    }

    private String getRuleDesc(ActivityRule activityRule) {
        ActivityType activityType = activityRule.getActivityType();
        StringBuffer ruleDesc = new StringBuffer();
        if (activityType == ActivityType.FULL_REDUCTION) {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionAmount())
                    .append("元减")
                    .append(activityRule.getBenefitAmount())
                    .append("元");
        } else {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionNum())
                    .append("元打")
                    .append(activityRule.getBenefitDiscount())
                    .append("折");
        }
        return ruleDesc.toString();
    }

    //获取购物车满足条件的促销与优惠券信息
    @Override
    public OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId) {
        List<CartInfoVo> carInfoVoList = this.findCartActivityList(cartInfoList);
        //促销活动优惠的总金额
        BigDecimal activityReduceAmount = carInfoVoList.stream()
                .filter(carInfoVo -> null != carInfoVo.getActivityRule())
                .map(carInfoVo -> carInfoVo.getActivityRule().getReduceAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //购物车可使用的优惠券列表
        List<CouponInfo> couponInfoList = couponInfoService.findCartCouponInfo(cartInfoList, userId);
        //优惠券可优惠的总金额，一次购物只能使用一张优惠券
        BigDecimal couponReduceAmount = new BigDecimal(0);
        if(!CollectionUtils.isEmpty(couponInfoList)) {
            couponReduceAmount = couponInfoList.stream()
                    .filter(couponInfo -> couponInfo.getIsOptimal().intValue() == 1)
                    .map(couponInfo -> couponInfo.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        //购物车原始总金额
        BigDecimal originalTotalAmount = cartInfoList.stream()
                .filter(cartInfo -> cartInfo.getIsChecked() == 1)
                .map(cartInfo -> cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //最终总金额
        BigDecimal totalAmount = originalTotalAmount.subtract(activityReduceAmount).subtract(couponReduceAmount);

        OrderConfirmVo orderTradeVo = new OrderConfirmVo();
        orderTradeVo.setCarInfoVoList(carInfoVoList);
        orderTradeVo.setActivityReduceAmount(activityReduceAmount);
        orderTradeVo.setCouponInfoList(couponInfoList);
        orderTradeVo.setCouponReduceAmount(couponReduceAmount);
        orderTradeVo.setOriginalTotalAmount(originalTotalAmount);
        orderTradeVo.setTotalAmount(totalAmount);
        return orderTradeVo;
    }

    //获取购物车对应规则数据，商品按规则分组
    @Override
    public List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList) {
        List<CartInfoVo> carInfoVoList = new ArrayList<>();

        //第一步：把购物车里面相同活动的购物项汇总一起
        //获取skuId列表
        List<Long> skuIdList = cartInfoList.stream().map(CartInfo::getSkuId).collect(Collectors.toList());
        //获取skuId列表对应的全部促销规则
        List<ActivitySku> activitySkuList = baseMapper.selectCartActivityList(skuIdList);
        //根据活动分组，取活动对应的skuId列表，即把购物车里面相同活动的购物项汇总一起，凑单使用
        Map<Long, Set<Long>> activityIdToSkuIdListMap = activitySkuList.stream().collect(Collectors.groupingBy(ActivitySku::getActivityId, Collectors.mapping(ActivitySku::getSkuId, Collectors.toSet())));

        //第二步：获取活动对应的促销规则
        //获取购物车对应的活动id
        Set<Long> activityIdSet = activitySkuList.stream().map(ActivitySku::getActivityId).collect(Collectors.toSet());
        Map<Long, List<ActivityRule>> activityIdToActivityRuleListMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(activityIdSet)) {
            LambdaQueryWrapper<ActivityRule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(ActivityRule::getConditionAmount, ActivityRule::getConditionNum);
            queryWrapper.in(ActivityRule::getActivityId, activityIdSet);
            List<ActivityRule> activityRuleList = activityRuleMapper.selectList(queryWrapper);
            //按活动Id分组，获取活动对应的规则
            activityIdToActivityRuleListMap = activityRuleList.stream().collect(Collectors.groupingBy(activityRule -> activityRule.getActivityId()));
        }

        //第三步：根据活动汇总购物项，相同活动的购物项为一组显示在页面，并且计算最优优惠金额
        //记录有活动的购物项skuId
        Set<Long> activitySkuIdSet = new HashSet<>();
        if(!CollectionUtils.isEmpty(activityIdToSkuIdListMap)) {
            Iterator<Map.Entry<Long, Set<Long>>> iterator = activityIdToSkuIdListMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Set<Long>> entry = iterator.next();
                Long activityId = entry.getKey();
                //当前活动对应的购物项skuId列表
                Set<Long> currentActivitySkuIdSet = entry.getValue();
                //当前活动对应的购物项列表
                List<CartInfo> currentActivityCartInfoList = cartInfoList.stream().filter(cartInfo -> currentActivitySkuIdSet.contains(cartInfo.getSkuId())).collect(Collectors.toList());

                //当前活动的总金额
                BigDecimal activityTotalAmount = this.computeTotalAmount(currentActivityCartInfoList);
                //当前活动的购物项总个数
                Integer activityTotalNum = this.computeCartNum(currentActivityCartInfoList);
                //计算当前活动对应的最优规则
                //活动当前活动对应的规则
                List<ActivityRule> currentActivityRuleList = activityIdToActivityRuleListMap.get(activityId);
                ActivityType activityType = currentActivityRuleList.get(0).getActivityType();
                ActivityRule optimalActivityRule = null;
                if (activityType == ActivityType.FULL_REDUCTION) { //满减
                    optimalActivityRule = this.computeFullReduction(activityTotalAmount, currentActivityRuleList);
                } else { //满量
                    optimalActivityRule = this.computeFullDiscount(activityTotalNum, activityTotalAmount, currentActivityRuleList);
                }

                //同一活动对应的购物项列表与对应优化规则
                CartInfoVo carInfoVo = new CartInfoVo();
                carInfoVo.setCartInfoList(currentActivityCartInfoList);
                carInfoVo.setActivityRule(optimalActivityRule);
                carInfoVoList.add(carInfoVo);
                //记录哪些购物项参与了活动
                activitySkuIdSet.addAll(currentActivitySkuIdSet);
            }
        }

        //第四步：无活动的购物项，每一项一组
        skuIdList.removeAll(activitySkuIdSet);
        if(!CollectionUtils.isEmpty(skuIdList)) {
            //获取skuId对应的购物项
            Map<Long, CartInfo> skuIdToCartInfoMap = cartInfoList.stream().collect(Collectors.toMap(CartInfo::getSkuId, CartInfo->CartInfo));
            for(Long skuId : skuIdList) {
                CartInfoVo carInfoVo = new CartInfoVo();
                carInfoVo.setActivityRule(null);
                List<CartInfo> currentCartInfoList = new ArrayList<>();
                currentCartInfoList.add(skuIdToCartInfoMap.get(skuId));
                carInfoVo.setCartInfoList(currentCartInfoList);
                carInfoVoList.add(carInfoVo);
            }
        }
        return carInfoVoList;
    }

    /**
     * 计算满量打折最优规则
     * @param totalNum
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠折扣从大到小排序了
     */
    private ActivityRule computeFullDiscount(Integer totalNum, BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项购买个数大于等于满减件数，则优化打折
            if (totalNum.intValue() >= activityRule.getConditionNum()) {
                BigDecimal skuDiscountTotalAmount = totalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                BigDecimal reduceAmount = totalAmount.subtract(skuDiscountTotalAmount);
                activityRule.setReduceAmount(reduceAmount);
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，还差")
                    .append(totalNum-optimalActivityRule.getConditionNum())
                    .append("件");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    /**
     * 计算满减最优规则
     * @param totalAmount
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
     */
    private ActivityRule computeFullReduction(BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项金额大于等于满减金额，则优惠金额
            if (totalAmount.compareTo(activityRule.getConditionAmount()) > -1) {
                //优惠后减少金额
                activityRule.setReduceAmount(activityRule.getBenefitAmount());
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，还差")
                    .append(totalAmount.subtract(optimalActivityRule.getConditionAmount()))
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    private int computeCartNum(List<CartInfo> cartInfoList) {
        int total = 0;
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                total += cartInfo.getSkuNum();
            }
        }
        return total;
    }
}
