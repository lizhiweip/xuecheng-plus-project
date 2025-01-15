package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @Author lzw
 * @Date 2025/1/15 18:26
 * @description 账号密码认证
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    private XcUserMapper xcUserMapper;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CheckCodeClient checkCodeClient;


    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();

        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
        if(checkcodekey ==null || checkcode==null){
            throw new RuntimeException("请输入验证码");
        }
        //远程调用验证码服务校验验证码校验码
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if(verify == null || !verify){
            throw new RuntimeException("验证码输入错误");
        }


        //校验账号
          //根据username账号查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
          //查询到用户不存在，返回null即可，spring security框架抛出异常用户不存在
        if(xcUser == null){
            throw new RuntimeException("账号不存在");
        }
        //密码是否正确
            //如果查到了用户拿到正确的密码，
        String passwordDb = xcUser.getPassword();
            //拿到用户输入的密码
        String passwordForm = authParamsDto.getPassword();
            //校验密码
        boolean matches = passwordEncoder.matches(passwordForm, passwordDb);
        if(!matches){
            throw new RuntimeException("账号或密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);

        return xcUserExt;
    }
}
