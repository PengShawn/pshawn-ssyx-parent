package com.psjava.ssyx.acl.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.psjava.ssyx.model.acl.Role;
import com.psjava.ssyx.vo.acl.RoleQueryVo;

import java.util.Map;

public interface RoleService extends IService<Role> {
    /**
     * 角色分页列表
     * @param pageParam
     * @param roleQueryVo
     * @return
     */
    IPage<Role> selectPage(Page<Role> pageParam, RoleQueryVo roleQueryVo);

    /**
     * 根据用户id查询后台用户被分配的角色
     * @param adminId
     * @return
     */
    Map<String, Object> getRoleByUserId(Long adminId);

    void saveAdminRoleRelationShip(Long adminId, Long[] roleIds);
}
