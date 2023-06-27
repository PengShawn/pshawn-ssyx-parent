package com.psjava.ssyx.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.psjava.ssyx.model.sys.Region;

import java.util.List;

/**
 * <p>
 * 地区表 服务类
 * </p>
 *
 * @author pshawn
 * @since 2023-06-27
 */
public interface RegionService extends IService<Region> {

    List<Region> findRegionByKeyword(String keyword);
}
