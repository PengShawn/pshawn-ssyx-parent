package com.psjava.ssyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psjava.ssyx.model.product.SkuAttrValue;
import com.psjava.ssyx.product.mapper.SkuAttrValueMapper;
import com.psjava.ssyx.product.service.SkuAttrValueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * spu属性值 服务实现类
 * </p>
 *
 * @author pshawn
 * @since 2023-06-28
 */
@Service
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValue> implements SkuAttrValueService {

    @Override
    public List<SkuAttrValue> findBySkuId(Long skuId) {
        return baseMapper.selectList(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId, skuId));
    }
}
