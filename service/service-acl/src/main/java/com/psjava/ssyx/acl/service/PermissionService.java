package com.psjava.ssyx.acl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.psjava.ssyx.model.acl.Permission;

import java.util.List;

public interface PermissionService extends IService<Permission> {
    //查询所有菜单
    List<Permission> queryAllMenu();

    //递归删除
    boolean removeChildById(Long id);
}
