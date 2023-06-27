package com.psjava.ssyx.sys.controller;


import com.psjava.ssyx.common.result.Result;
import com.psjava.ssyx.sys.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 地区表 前端控制器
 * </p>
 *
 * @author pshawn
 * @since 2023-06-27
 */
@Api(tags = "区域接口")
@RestController
@RequestMapping("/admin/sys/region")
@CrossOrigin
public class RegionController {
    @Autowired
    private RegionService regionService;

    @ApiOperation("根据关键字获取区域列表")
    @GetMapping("findRegionByKeyword/{keyword}")
    public Result findRegionByKeyword(@PathVariable String keyword) {
        return Result.ok(regionService.findRegionByKeyword(keyword));
    }
}

