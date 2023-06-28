package com.psjava.ssyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psjava.ssyx.model.product.SkuPoster;
import com.psjava.ssyx.product.mapper.SkuPosterMapper;
import com.psjava.ssyx.product.service.SkuPosterService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 商品海报表 服务实现类
 * </p>
 *
 * @author pshawn
 * @since 2023-06-28
 */
@Service
public class SkuPosterServiceImpl extends ServiceImpl<SkuPosterMapper, SkuPoster> implements SkuPosterService {

    @Override
    public List<SkuPoster> findBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuPoster> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuPoster::getSkuId, skuId);
        return baseMapper.selectList(wrapper);
    }
}
