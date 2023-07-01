package com.psjava.ssyx.product.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psjava.ssyx.common.result.Result;
import com.psjava.ssyx.model.activity.CouponInfo;
import com.psjava.ssyx.product.service.CouponInfoService;
import com.psjava.ssyx.vo.activity.CouponRuleVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 优惠券信息 前端控制器
 * </p>
 *
 * @author pshawn
 * @since 2023-06-30
 */
@Api(tags = "优惠券接口")
@RestController
@RequestMapping("/admin/activity/couponInfo")
@CrossOrigin
public class CouponInfoController {
    @Autowired
    private CouponInfoService couponInfoService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result list(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit
    ) {
        Page<CouponInfo> pageParam = new Page<>(page, limit);
        IPage<CouponInfo> pageModel = couponInfoService.selectPage(pageParam);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取优惠券")
    @GetMapping("get/{id}")
    public Result get(@PathVariable String id) {
        CouponInfo couponInfo = couponInfoService.getCouponInfo(id);
        return Result.ok(couponInfo);
    }

    @ApiOperation(value = "新增优惠券")
    @PostMapping("save")
    public Result save(@RequestBody CouponInfo couponInfo) {
        couponInfoService.save(couponInfo);
        return Result.ok(null);
    }

    @ApiOperation(value = "修改优惠券")
    @PutMapping("update")
    public Result updateById(@RequestBody CouponInfo couponInfo) {
        couponInfoService.updateById(couponInfo);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除优惠券")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable String id) {
        couponInfoService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation(value="根据id列表删除优惠券")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<String> idList){
        couponInfoService.removeByIds(idList);
        return Result.ok(null);
    }

    @ApiOperation(value = "获取优惠券信息")
    @GetMapping("findCouponRuleList/{id}")
    public Result findActivityRuleList(@PathVariable Long id) {
        return Result.ok(couponInfoService.findCouponRuleList(id));
    }

    @ApiOperation(value = "新增优惠券规则")
    @PostMapping("saveCouponRule")
    public Result saveCouponRule(@RequestBody CouponRuleVo couponRuleVo) {
        couponInfoService.saveCouponRule(couponRuleVo);
        return Result.ok(null);
    }

    @ApiOperation(value = "根据关键字获取sku列表，活动使用")
    @GetMapping("findCouponByKeyword/{keyword}")
    public Result findCouponByKeyword(@PathVariable("keyword") String keyword) {
        return Result.ok(couponInfoService.findCouponByKeyword(keyword));
    }
}

