package com.psjava.ssyx.product.service;

import com.psjava.ssyx.model.product.Attr;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 商品属性 服务类
 * </p>
 *
 * @author pshawn
 * @since 2023-06-28
 */
public interface AttrService extends IService<Attr> {

    // 根据属性分组id 获取属性列表
    List<Attr> findByAttrGroupId(Long attrGroupId);
}
