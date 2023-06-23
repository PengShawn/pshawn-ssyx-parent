package com.psjava.ssyx.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psjava.ssyx.acl.mapper.AdminRoleMapper;
import com.psjava.ssyx.acl.service.AdminRoleService;
import com.psjava.ssyx.acl.service.RoleService;
import com.psjava.ssyx.model.acl.Admin;
import com.psjava.ssyx.model.acl.AdminRole;
import com.psjava.ssyx.model.acl.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminRoleServiceImpl extends ServiceImpl<AdminRoleMapper, AdminRole> implements AdminRoleService {
    @Autowired
    private RoleService roleService;

    @Override
    public Map<String, Object> getRoleByUserId(Long adminId) {
        // 查询所有角色
        List<Role> allRoleList = roleService.list();
        // 根据用户id查询用户分配角色列表
        List<AdminRole> existAdminRoleList = baseMapper.selectList(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getAdminId, adminId).select(AdminRole::getRoleId));

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
        baseMapper.delete(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getAdminId, adminId));

        List<AdminRole> adminRoleList = new ArrayList<>();
        for(Long roleId : roleIds) {
            AdminRole adminRole = new AdminRole();
            adminRole.setAdminId(adminId);
            adminRole.setRoleId(roleId);
            adminRoleList.add(adminRole);
        }
        saveBatch(adminRoleList);
    }
}
