package com.psjava.ssyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psjava.ssyx.client.product.ProductFeignClient;
import com.psjava.ssyx.enums.ActivityType;
import com.psjava.ssyx.model.activity.ActivityInfo;
import com.psjava.ssyx.model.activity.ActivityRule;
import com.psjava.ssyx.model.activity.ActivitySku;
import com.psjava.ssyx.model.product.SkuInfo;
import com.psjava.ssyx.product.mapper.ActivityInfoMapper;
import com.psjava.ssyx.product.mapper.ActivityRuleMapper;
import com.psjava.ssyx.product.mapper.ActivitySkuMapper;
import com.psjava.ssyx.product.service.ActivityInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psjava.ssyx.vo.activity.ActivityRuleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
}
