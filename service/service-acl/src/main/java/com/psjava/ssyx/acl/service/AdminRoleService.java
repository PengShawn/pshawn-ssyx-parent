package com.psjava.ssyx.acl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.psjava.ssyx.model.acl.AdminRole;

import java.util.Map;

public interface AdminRoleService extends IService<AdminRole> {
    /**
     * 根据用户id查询后台用户被分配的角色
     * @param adminId
     * @return
     */
    Map<String, Object> getRoleByUserId(Long adminId);

    void saveAdminRoleRelationShip(Long adminId, Long[] roleIds);
}
