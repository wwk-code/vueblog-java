package com.markerhub.shiro;

import com.markerhub.entity.User;
import com.markerhub.service.UserService;
import com.markerhub.util.JwtUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import cn.hutool.core.bean.BeanUtil;


/**
 *    其实在本项目中主要就是doGetAuthenticationInfo登录认证这个方法，可以看到我们通过jwt获取到用户信息，
 *  判断用户的状态，最后异常就抛出对应的异常信息，否者封装成SimpleAuthenticationInfo返回给shiro
 */

@Component
public class AccountRealm extends AuthorizingRealm {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserService userService;

    // 为了让realm支持jwt的凭证校验
    //shiro默认supports的是 AuthenticationToken，而我们现在采用了jwt的方式，所以我们自定义一个JwtToken，来完成shiro的supports方法
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }


    //权限校验(未做权限认证)
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }


    //登录认证校验
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        JwtToken jwtToken = (JwtToken) token;

        String userId = jwtUtils.getClaimByToken((String)jwtToken.getPrincipal()).getSubject();

        User user = userService.getById(Long.valueOf(userId));

        if(user == null){
            throw new UnknownAccountException("账户不存在");
        }

        if(user.getStatus() == -1){
            throw new LockedAccountException("账号已被锁定");
        }

        AccountProfile profile = new AccountProfile();
        BeanUtil.copyProperties(user,profile);

        System.out.println("-----------------");

        return new SimpleAuthenticationInfo(profile, jwtToken.getCredentials(), getName());

    }
}















