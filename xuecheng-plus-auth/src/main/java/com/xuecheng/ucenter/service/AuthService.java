package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @Author lzw
 * @Date 2025/1/15 18:23
 * @description 统一认证的接口
 */
public interface AuthService {
    /**
     * 认证方法
     * @param authParamsDto 认证参数
     * @return
     */
    XcUserExt execute(AuthParamsDto authParamsDto);

}
