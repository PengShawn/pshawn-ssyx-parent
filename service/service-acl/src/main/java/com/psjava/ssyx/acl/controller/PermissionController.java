package com.psjava.ssyx.acl.controller;

import com.psjava.ssyx.acl.service.PermissionService;
import com.psjava.ssyx.common.result.Result;
import com.psjava.ssyx.model.acl.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/acl/permission")
@Api(tags = "菜单管理")
//@CrossOrigin
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

    @ApiOperation("查询所有菜单")
    @GetMapping
    public Result list() {
        return Result.ok(permissionService.queryAllMenu());
    }

    @ApiOperation("新增菜单")
    @PostMapping("save")
    public Result save(@RequestBody Permission permission) {
        return permissionService.save(permission)? Result.ok(null) : Result.fail(null);
    }

    @ApiOperation("修改菜单")
    @PutMapping("update")
    public Result updateById(@RequestBody Permission permission) {
        return permissionService.updateById(permission)? Result.ok(null) : Result.fail(null);
    }

    @ApiOperation("递归删除菜单")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        return permissionService.removeChildById(id)? Result.ok(null) : Result.fail(null);
    }
}
