package com.markerhub.controller;


import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.markerhub.common.dto.LoginDto;
import com.markerhub.common.lang.Result;
import com.markerhub.entity.User;
import com.markerhub.service.UserService;
import com.markerhub.util.JwtUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;


@RestController
public class AccountController {

    @Autowired
    UserService userService;

    @Autowired
    JwtUtils jwtUtils;


    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response) {

        User user = userService.getOne(new QueryWrapper<User>().eq("username", loginDto.getUsername()));
        Assert.notNull(user,"用户不存在");

        // 数据库中的密码数据已经是MD5加密过了的，这里直接把DTO中的数据以同样的方式加密再作比较
        if(!user.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))){
            return Result.fail("密码不正确");
        }

        String jwt = jwtUtils.generateToken(user.getId());

        response.setHeader("Authorization",jwt);  // 把jwt数据存入到Header中
        response.setHeader("Access-control-Expose-Headers","Authorization");

        return Result.succ(MapUtil.builder()
                    .put("id",user.getId())
                    .put("username",user.getUsername())
                    .put("avatar",user.getAvatar())
                    .put("email",user.getEmail())
                    .map()
        );

    }



    @RequiresAuthentication  //验证用户是否登录，会走
    @GetMapping("/logout")
    public Result logout() {
        SecurityUtils.getSubject().logout();  // Shiro完成退出，清空用户信息
        return Result.succ(null);
    }



}
