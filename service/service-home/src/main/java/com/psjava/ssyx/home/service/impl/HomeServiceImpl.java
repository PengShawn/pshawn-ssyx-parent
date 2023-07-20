package com.psjava.ssyx.home.service.impl;

import com.psjava.ssyx.client.activity.ActivityFeignClient;
import com.psjava.ssyx.client.product.ProductFeignClient;
import com.psjava.ssyx.client.search.SkuFeignClient;
import com.psjava.ssyx.client.user.UserFeignClient;
import com.psjava.ssyx.home.service.HomeService;
import com.psjava.ssyx.model.product.Category;
import com.psjava.ssyx.model.product.SkuInfo;
import com.psjava.ssyx.model.search.SkuEs;
import com.psjava.ssyx.vo.user.LeaderAddressVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomeServiceImpl implements HomeService {
    @Resource
    private ProductFeignClient productFeignClient;

//    @Resource
//    private SeckillFeignClient seckillFeignClient;

    @Resource
    private SkuFeignClient skuFeignClient;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private ActivityFeignClient activityFeignClient;

    @Override
    public Map<String, Object> home(Long userId) {
        Map<String, Object> result = new HashMap<>();

        //获取分类信息
        List<Category> categoryList = productFeignClient.findAllCategoryList();
        result.put("categoryList", categoryList);

        //获取新人专享商品
        List<SkuInfo> newPersonSkuInfoList =  productFeignClient.findNewPersonSkuInfoList();
        result.put("newPersonSkuInfoList", newPersonSkuInfoList);

        //TODO 获取用户首页秒杀数据

        //提货点地址信息
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        result.put("leaderAddressVo", leaderAddressVo);

        //获取爆款商品
        List<SkuEs> hotSkuList = skuFeignClient.findHotSkuList();
        //获取sku对应的促销活动标签
        if(!CollectionUtils.isEmpty(hotSkuList)) {
            List<Long> skuIdList = hotSkuList.stream().map(sku -> sku.getId()).collect(Collectors.toList());
            Map<Long, List<String>> skuIdToRuleListMap = activityFeignClient.findActivity(skuIdList);
            if(null != skuIdToRuleListMap) {
                hotSkuList.forEach(skuEs -> {
                    skuEs.setRuleList(skuIdToRuleListMap.get(skuEs.getId()));
                });
            }
        }
        result.put("hotSkuList", hotSkuList);
        return result;
    }
}