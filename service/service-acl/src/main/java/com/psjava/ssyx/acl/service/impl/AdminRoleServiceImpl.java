package com.psjava.ssyx.acl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psjava.ssyx.acl.mapper.AdminRoleMapper;
import com.psjava.ssyx.acl.service.AdminRoleService;
import com.psjava.ssyx.model.acl.AdminRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminRoleServiceImpl extends ServiceImpl<AdminRoleMapper, AdminRole> implements AdminRoleService {
}
