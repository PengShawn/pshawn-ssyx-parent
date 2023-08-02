package com.psjava.ssyx.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psjava.ssyx.activity.mapper.CouponInfoMapper;
import com.psjava.ssyx.activity.mapper.CouponRangeMapper;
import com.psjava.ssyx.activity.mapper.CouponUseMapper;
import com.psjava.ssyx.activity.service.CouponInfoService;
import com.psjava.ssyx.client.product.ProductFeignClient;
import com.psjava.ssyx.enums.CouponRangeType;
import com.psjava.ssyx.enums.CouponStatus;
import com.psjava.ssyx.model.activity.CouponInfo;
import com.psjava.ssyx.model.activity.CouponRange;
import com.psjava.ssyx.model.activity.CouponUse;
import com.psjava.ssyx.model.order.CartInfo;
import com.psjava.ssyx.model.product.Category;
import com.psjava.ssyx.model.product.SkuInfo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psjava.ssyx.vo.activity.CouponRuleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 优惠券信息 服务实现类
 * </p>
 *
 * @author pshawn
 * @since 2023-06-30
 */
@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {
    @Autowired
    private CouponRangeMapper couponRangeMapper;
    @Autowired
    private CouponUseMapper couponUseMapper;
    @Autowired
    private ProductFeignClient productFeignClient;

    //优惠卷分页查询
    @Override
    public IPage selectPage(Page<CouponInfo> pageParam) {
        IPage<CouponInfo> page = baseMapper.selectPage(pageParam, null);
        page.getRecords().forEach(item -> {
            item.setCouponTypeString(item.getCouponType().getComment());
            if(null != item.getRangeType()) {
                item.setRangeTypeString(item.getRangeType().getComment());
            }
        });
        return page;
    }

    //根据id获取优惠券
    @Override
    public CouponInfo getCouponInfo(String id) {
        CouponInfo couponInfo = this.getById(id);
        couponInfo.setCouponTypeString(couponInfo.getCouponType().getComment());
        if(null != couponInfo.getRangeType()) {
            couponInfo.setRangeTypeString(couponInfo.getRangeType().getComment());
        }
        return couponInfo;
    }

    //根据优惠卷id获取优惠券规则列表
    @Override
    public Map<String, Object> findCouponRuleList(Long couponId) {
        Map<String, Object> result = new HashMap<>();
        CouponInfo couponInfo = this.getById(couponId);

        List<CouponRange> activitySkuList = couponRangeMapper.selectList(new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId, couponId));
        List<Long> rangeIdList = activitySkuList.stream().map(CouponRange::getRangeId).collect(Collectors.toList());

        if(!CollectionUtils.isEmpty(rangeIdList)) {
            if(couponInfo.getRangeType() == CouponRangeType.SKU) {
                List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(rangeIdList);
                result.put("skuInfoList", skuInfoList);
            } else if (couponInfo.getRangeType() == CouponRangeType.CATEGORY) {
                List<Category> categoryList = productFeignClient.findCategoryList(rangeIdList);
                result.put("categoryList", categoryList);
            } else {
                //通用
            }
        }
        return result;
    }

    //新增优惠券规则
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCouponRule(CouponRuleVo couponRuleVo) {
        /*
        优惠券couponInfo 与 couponRange 要一起操作：先删除couponRange ，更新couponInfo ，再新增couponRange ！
         */
        couponRangeMapper.delete(
                new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId, couponRuleVo.getCouponId())
        );

        // 更新优惠券基本信息
        CouponInfo couponInfo = this.getById(couponRuleVo.getCouponId());
        // couponInfo.setCouponType();
        couponInfo.setRangeType(couponRuleVo.getRangeType());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setAmount(couponRuleVo.getAmount());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setRangeDesc(couponRuleVo.getRangeDesc());
        baseMapper.updateById(couponInfo);

        //  插入优惠券的规则 couponRangeList
        List<CouponRange> couponRangeList = couponRuleVo.getCouponRangeList();
        for (CouponRange couponRange : couponRangeList) {
            couponRange.setCouponId(couponRuleVo.getCouponId());
            //  插入数据
            couponRangeMapper.insert(couponRange);
        }
    }

    //根据关键字获取sku列表，活动使用
    @Override
    public List<CouponInfo> findCouponByKeyword(String keyword) {
        return baseMapper.selectList(new LambdaQueryWrapper<CouponInfo>().like(CouponInfo::getCouponName, keyword));
    }

    //根据skuid和userid查询优惠券信息
    @Override
    public List<CouponInfo> findCouponInfoList(Long skuId, Long userId) {
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if(null == skuInfo) return new ArrayList<>();
        return baseMapper.selectCouponInfoList(skuInfo.getId(), skuInfo.getCategoryId(), userId);
    }

    //获取购物车中可使用优惠券列表
    @Override
    public List<CouponInfo> findCartCouponInfo(List<CartInfo> cartInfoList, Long userId) {
        //获取全部用户优惠券
        List<CouponInfo> userAllCouponInfoList = baseMapper.selectCartCouponInfoList(userId);
        if(CollectionUtils.isEmpty(userAllCouponInfoList)) return null;

        //获取优惠券id列表
        List<Long> couponIdList = userAllCouponInfoList.stream().map(couponInfo -> couponInfo.getId()).collect(Collectors.toList());
        //查询优惠券对应的范围
        List<CouponRange> couponRangesList = couponRangeMapper.selectList(new LambdaQueryWrapper<CouponRange>().in(CouponRange::getCouponId, couponIdList));
        //获取优惠券id对应的满足使用范围的购物项skuId列表
        Map<Long, List<Long>> couponIdToSkuIdMap = this.findCouponIdToSkuIdMap(cartInfoList, couponRangesList);
        //优惠后减少金额
        BigDecimal reduceAmount = new BigDecimal("0");
        //记录最优优惠券
        CouponInfo optimalCouponInfo = null;
        for(CouponInfo couponInfo : userAllCouponInfoList) {
            if(CouponRangeType.ALL == couponInfo.getRangeType()) {
                //全场通用
                //判断是否满足优惠使用门槛
                //计算购物车商品的总价
                BigDecimal totalAmount = computeTotalAmount(cartInfoList);
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            } else {
                //优惠券id对应的满足使用范围的购物项skuId列表
                List<Long> skuIdList = couponIdToSkuIdMap.get(couponInfo.getId());
                //当前满足使用范围的购物项
                List<CartInfo> currentCartInfoList = cartInfoList.stream().filter(cartInfo -> skuIdList.contains(cartInfo.getSkuId())).collect(Collectors.toList());
                BigDecimal totalAmount = computeTotalAmount(currentCartInfoList);
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            }
            if (couponInfo.getIsSelect().intValue() == 1 && couponInfo.getAmount().subtract(reduceAmount).doubleValue() > 0) {
                reduceAmount = couponInfo.getAmount();
                optimalCouponInfo = couponInfo;
            }
        }
        if(null != optimalCouponInfo) {
            optimalCouponInfo.setIsOptimal(1);
        }
        return userAllCouponInfoList;
    }
    //获取优惠券范围对应的购物车列表
    @Override
    public CouponInfo findRangeSkuIdList(List<CartInfo> cartInfoList, Long couponId) {
        CouponInfo couponInfo = this.getById(couponId);
        if(null == couponInfo || couponInfo.getCouponStatus().intValue() == 2) return null;

        //查询优惠券对应的范围
        List<CouponRange> couponRangesList = couponRangeMapper.selectList(new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId, couponId));
        //获取优惠券id对应的满足使用范围的购物项skuId列表
        Map<Long, List<Long>> couponIdToSkuIdMap = this.findCouponIdToSkuIdMap(cartInfoList, couponRangesList);
        List<Long> skuIdList = couponIdToSkuIdMap.entrySet().iterator().next().getValue();
        couponInfo.setSkuIdList(skuIdList);
        return couponInfo;
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

    //获取优惠券id对应的满足使用范围的购物项skuId列表
    //说明：一个优惠券可能有多个购物项满足它的使用范围，那么多个购物项可以拼单使用这个优惠券
    private Map<Long, List<Long>> findCouponIdToSkuIdMap(List<CartInfo> cartInfoList, List<CouponRange> couponRangesList) {
        Map<Long, List<Long>> couponIdToSkuIdMap = new HashMap<>();
        //优惠券id对应的范围列表
        Map<Long, List<CouponRange>> couponIdToCouponRangeListMap = couponRangesList.stream().collect(Collectors.groupingBy(couponRange -> couponRange.getCouponId()));
        Iterator<Map.Entry<Long, List<CouponRange>>> iterator = couponIdToCouponRangeListMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, List<CouponRange>> entry = iterator.next();
            Long couponId = entry.getKey();
            List<CouponRange> couponRangeList = entry.getValue();

            Set<Long> skuIdSet = new HashSet<>();
            for (CartInfo cartInfo : cartInfoList) {
                for(CouponRange couponRange : couponRangeList) {
                    if(CouponRangeType.SKU == couponRange.getRangeType() && couponRange.getRangeId().longValue() == cartInfo.getSkuId().intValue()) {
                        skuIdSet.add(cartInfo.getSkuId());
                    } else if(CouponRangeType.CATEGORY == couponRange.getRangeType() && couponRange.getRangeId().longValue() == cartInfo.getCategoryId().intValue()) {
                        skuIdSet.add(cartInfo.getSkuId());
                    } else {

                    }
                }
            }
            couponIdToSkuIdMap.put(couponId, new ArrayList<>(skuIdSet));
        }
        return couponIdToSkuIdMap;
    }

    @Override
    public void updateCouponInfoUseStatus(Long couponId, Long userId, Long orderId) {
        CouponUse couponUse = new CouponUse();
        couponUse.setOrderId(orderId);
        couponUse.setCouponStatus(CouponStatus.USED);
        couponUse.setUsingTime(new Date());

        LambdaQueryWrapper<CouponUse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponUse::getCouponId, couponId);
        queryWrapper.eq(CouponUse::getUserId, userId);
        couponUseMapper.update(couponUse, queryWrapper);
    }
}
