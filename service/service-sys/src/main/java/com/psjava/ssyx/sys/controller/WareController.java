package com.psjava.ssyx.sys.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psjava.ssyx.common.result.Result;
import com.psjava.ssyx.model.sys.Ware;
import com.psjava.ssyx.sys.service.WareService;
import com.psjava.ssyx.vo.product.WareQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 仓库表 前端控制器
 * </p>
 *
 * @author pshawn
 * @since 2023-06-27
 */
@Api(tags = "仓库接口")
@RestController
@RequestMapping("/admin/sys/ware")
public class WareController {
    @Autowired
    private WareService wareService;

    @ApiOperation("获取全部仓库")
    @GetMapping("findAllList")
    public Result findAllList() {
        return Result.ok(wareService.list());
    }

    @ApiOperation("删除仓库")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        wareService.removeById(id);
        return Result.ok(null);
    }
}

