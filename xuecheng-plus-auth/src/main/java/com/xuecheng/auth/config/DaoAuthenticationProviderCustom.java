package com.xuecheng.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * @Author lzw
 * @Date 2025/1/15 18:11
 * @description 重写了原来的DaoAuthenticationProviderCustom校验密码的方法，因为我们统一认证入口，有一些认证方式不需要不需要校验密码
 */
@Component
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {
    @Autowired
    public  void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }
}
