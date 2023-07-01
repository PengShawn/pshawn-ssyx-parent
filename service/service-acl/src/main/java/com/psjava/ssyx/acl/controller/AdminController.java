package com.psjava.ssyx.acl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psjava.ssyx.acl.service.AdminRoleService;
import com.psjava.ssyx.acl.service.AdminService;
import com.psjava.ssyx.common.utils.MD5;
import com.psjava.ssyx.model.acl.Admin;
import com.psjava.ssyx.vo.acl.AdminQueryVo;
import com.psjava.ssyx.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "后台用户接口")
@RestController
@RequestMapping("/admin/acl/user")
//@CrossOrigin
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private AdminRoleService adminRoleService;

    @ApiOperation("后台用户列表")
    @GetMapping("{page}/{limit}")
    public Result list(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,

            @ApiParam(name = "adminQueryVo", value = "查询对象", required = false)
            AdminQueryVo adminQueryVo) {
        Page<Admin> pageParam = new Page<>(page, limit);
        IPage<Admin> pageModel = adminService.selectPage(pageParam, adminQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation("根据id查询某个后台用户")
    @GetMapping("get/{id}")
    public Result<Admin> get(@PathVariable Long id) {
        return Result.ok(adminService.getById(id));
    }

    @ApiOperation("添加后台用户")
    @PostMapping("save")
    public Result save(@RequestBody Admin admin) {
        admin.setPassword(MD5.encrypt(admin.getPassword()));
        return adminService.save(admin)? Result.ok(null) : Result.fail(null);
    }

    @ApiOperation("修改后台用户")
    @PutMapping("update")
    public Result update(@RequestBody Admin admin) {
        return adminService.updateById(admin)? Result.ok(null) : Result.fail(null);
    }

    @ApiOperation("删除后台用户")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        return adminService.removeById(id)? Result.ok(null) : Result.fail(null);
    }

    @ApiOperation("根据id列表删除后台用户")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> ids) {
        return adminService.removeByIds(ids)? Result.ok(null) : Result.fail(null);
    }

    @ApiOperation("根据后台用户id获取角色数据")
    @GetMapping("/toAssign/{adminId}")
    public Result toAssign(@PathVariable Long adminId) {
        Map<String, Object> roleMap = adminRoleService.getRoleByUserId(adminId);
        return Result.ok(roleMap);
    }

    @ApiOperation(value = "为后台用户进行角色分配")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestParam Long adminId,@RequestParam Long[] roleId) {
        adminRoleService.saveAdminRoleRelationShip(adminId, roleId);
        return Result.ok(null);
    }
}
