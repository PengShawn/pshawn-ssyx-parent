package com.psjava.ssyx.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.psjava.ssyx.model.user.User;
import com.psjava.ssyx.vo.user.LeaderAddressVo;
import com.psjava.ssyx.vo.user.UserLoginVo;

public interface UserService extends IService<User> {

    LeaderAddressVo getLeaderAddressVoByUserId(Long userId);

    //根据微信openid获取用户信息
    User getByOpenid(String openId);

    //获取当前登录用户信息
    UserLoginVo getUserLoginVo(Long userId);
}
