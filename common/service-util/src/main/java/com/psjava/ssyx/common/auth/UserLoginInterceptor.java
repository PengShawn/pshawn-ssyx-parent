package com.psjava.ssyx.common.auth;

import com.psjava.ssyx.common.constant.RedisConst;
import com.psjava.ssyx.common.utils.JwtHelper;
import com.psjava.ssyx.vo.user.UserLoginVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserLoginInterceptor implements HandlerInterceptor {
    private RedisTemplate redisTemplate;

    public UserLoginInterceptor(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        this.initUserLoginVo(request);
        return true;
    }

    private void initUserLoginVo(HttpServletRequest request){
        //从请求头获取token
        String token = request.getHeader("token");
        if (!StringUtils.isEmpty(token)) {
            Long userId = JwtHelper.getUserId(token);
            UserLoginVo userLoginVo = (UserLoginVo)redisTemplate.opsForValue().get(RedisConst.USER_LOGIN_KEY_PREFIX + userId);
            if(userLoginVo != null) {
                //将UserInfo放入上下文中
                AuthContextHolder.setUserId(userLoginVo.getUserId());
                AuthContextHolder.setWareId(userLoginVo.getWareId());
                AuthContextHolder.setUserLoginVo(userLoginVo);
            }
        }
    }
}
