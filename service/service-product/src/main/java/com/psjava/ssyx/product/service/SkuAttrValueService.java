package com.psjava.ssyx.product.service;

import com.psjava.ssyx.model.product.SkuAttrValue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * spu属性值 服务类
 * </p>
 *
 * @author pshawn
 * @since 2023-06-28
 */
public interface SkuAttrValueService extends IService<SkuAttrValue> {

    List<SkuAttrValue> findBySkuId(Long skuId);
}
