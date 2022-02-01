package com.markerhub.shiro;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;


// shiro默认supports的是 AuthenticationToken ，而我们现在采用了jwt的方式，所以这里我们自定义一个JwtToken，来完成shiro的supports方法
public class JwtToken implements AuthenticationToken {

    private String token;

    public JwtToken(String jwt){
        this.token = jwt;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
