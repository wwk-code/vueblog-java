package com.markerhub.shiro;

import cn.hutool.json.JSONUtil;
import com.markerhub.common.lang.Result;

import com.markerhub.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;

import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;


import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * createToken：实现登录，我们需要生成我们自定义支持的JwtToken
 * onAccessDenied：拦截校验，当头部没有Authorization时候，我们直接通过，不需要自动登录；当带有的时候，首先我们校验jwt的有效性，
 *                                                              没问题我们就直接执行executeLogin方法实现自动登录
 * onLoginFailure：登录异常时候进入的方法，我们直接把异常信息封装然后抛出
 * preHandle：拦截器的前置拦截，因为我们是前后端分析项目，项目中除了需要跨域全局配置之外，我们再拦截器中也需要提供跨域支持。
 */

// 自定义了一个过滤器，继承 Shiro框架的 AuthenticatingFilter 来做数据校验
public class JwtFilter extends AuthenticatingFilter{

    @Autowired
    JwtUtils jwtUtils;

    //生成Token
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");
        if(StringUtils.isEmpty(jwt)) {
            return null;
        }
        return new JwtToken(jwt);
    }

    //未登录时的处理
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt =  request.getHeader("Authorization");
        if(StringUtils.isEmpty(jwt)){   // 如果没有包含 请求的Header中没有包含jwt，则代表此时没有通过用户认证，则拒绝请求
            return true;
        }else {
            //校验jwt
            Claims claim = jwtUtils.getClaimByToken(jwt);
            if(claim == null || jwtUtils.isTokenExpired(claim.getExpiration())){
                throw new ExpiredCredentialsException("token已失效，请重新登录");
            }

            //在shiro中执行登录
            return executeLogin(servletRequest,servletResponse);
        }
    }


    //处理登录失败的情况
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        Throwable throwable = e.getCause()==null? e:e.getCause();

        Result result = Result.fail(throwable.getMessage());

        String json = JSONUtil.toJsonStr(result);

        try{
            httpServletResponse.getWriter().print(json);
        }catch (IOException ioException){

        }

        return false;

    }


    //解决跨域问题，都是死代码，可直接复制粘贴
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {

        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }

        return super.preHandle(request, response);
    }


}




