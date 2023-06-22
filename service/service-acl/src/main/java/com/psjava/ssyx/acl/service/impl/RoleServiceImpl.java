package com.psjava.ssyx.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psjava.ssyx.acl.mapper.RoleMapper;
import com.psjava.ssyx.acl.service.AdminRoleService;
import com.psjava.ssyx.acl.service.RoleService;
import com.psjava.ssyx.model.acl.AdminRole;
import com.psjava.ssyx.model.acl.Role;
import com.psjava.ssyx.vo.acl.RoleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private AdminRoleService adminRoleService;

    @Override
    public IPage<Role> selectPage(Page<Role> pageParam, RoleQueryVo roleQueryVo) {
        // 获取条件值：角色名称
        String roleName = roleQueryVo.getRoleName();
        // 创建条件构造器对象
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        // 判断条件值是否为空
        if(!StringUtils.isEmpty(roleName)) {
            wrapper.like(Role::getRoleName, roleName);
        }
        // 调用mapper方法实现条件分页查询
        IPage<Role> pageModel = baseMapper.selectPage(pageParam, wrapper);
        return pageModel;
    }

    @Override
    public Map<String, Object> getRoleByUserId(Long adminId) {
        // 查询所有角色
        List<Role> allRoleList = baseMapper.selectList(null);
        // 根据用户id查询用户分配角色列表
        List<AdminRole> existAdminRoleList = adminRoleService.list(new QueryWrapper<AdminRole>().eq("admin_id", adminId).select("role_id"));
        //List<AdminRole> existAdminRoleList = adminRoleService.list(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getAdminId, adminId));

        // 获取所有角色id的列表
        List<Long> roleIdList = existAdminRoleList.stream().map(AdminRole::getRoleId).collect(Collectors.toList());

        // 遍历所有角色列表，得到每个角色详细信息
        List<Role> assignRolesList = new ArrayList<>();
        for (Role role : allRoleList) {
            if(roleIdList.contains(role.getId())) {
                assignRolesList.add(role);
            }
        }
        // 封装到map
        Map<String, Object> result = new HashMap<>();
        result.put("allRolesList", allRoleList);
        result.put("assignRoles", assignRolesList);
        return result;
    }

    @Override
    public void saveAdminRoleRelationShip(Long adminId, Long[] roleIds) {
        adminRoleService.remove(new QueryWrapper<AdminRole>().eq("admin_id", adminId));

        List<AdminRole> adminRoleList = new ArrayList<>();
        for(Long roleId : roleIds) {
            AdminRole adminRole = new AdminRole();
            adminRole.setAdminId(adminId);
            adminRole.setRoleId(roleId);
            adminRoleList.add(adminRole);
        }
        adminRoleService.saveBatch(adminRoleList);
    }
}
