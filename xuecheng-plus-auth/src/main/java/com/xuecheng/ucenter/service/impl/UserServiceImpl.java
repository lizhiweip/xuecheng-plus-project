package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lzw
 * @Date 2025/1/14 20:21
 * @description
 */
@Component
@Slf4j
public class UserServiceImpl implements UserDetailsService {
   @Autowired
    XcUserMapper xcUserMapper;
   @Autowired
    XcMenuMapper xcMenuMapper;

   @Autowired
   ApplicationContext applicationContext;

   //传入的请求参数就是AuthParamsDto
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //将传入的json转成AuthParamsDto
        AuthParamsDto authParamsDto = null;
        try{
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        }catch (Exception e){
            throw new RuntimeException("请求认证的参数不符合要求");
        }
        //认证类型，有password，wx
        String authType = authParamsDto.getAuthType();

        //根据认证的类型，从spring容器当中取出指定的bean
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        //调用统一的execute方法
        XcUserExt execute = authService.execute(authParamsDto);
        //封装xcUserExt用户的信息UserDetails

        UserDetails userPrincipal = getUserPrincipal(execute);


        return userPrincipal;
    }

    /**
     * 查询用户信息
     * @param user 用户id，主键
     * @return
     */
    public UserDetails getUserPrincipal(XcUserExt user){
        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        //根据用户id查询用户权限
        String[] authorities = {"test"};
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(user.getId());
        if(xcMenus.size()>0){
            List<String> permissions = new ArrayList<>();
            xcMenus.forEach(m->{
                //拿到用户拥有的权限标识符
                permissions.add(m.getCode());
            });
            //将list转成数组
            authorities = permissions.toArray(new String[0]);
        }
        String password = user.getPassword();
        //为了安全在令牌中不放密码
        user.setPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(user);
        //创建UserDetails对象
        UserDetails userDetails = User.withUsername(userString).password(password ).authorities(authorities).build();
        return userDetails;
    }

}
