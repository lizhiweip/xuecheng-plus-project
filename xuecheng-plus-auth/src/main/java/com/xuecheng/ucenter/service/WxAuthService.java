package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

/**
 * @Author lzw
 * @Date 2025/1/16 18:16
 * @description 微信扫码接入
 */
public interface WxAuthService {
    /**
     * 微信扫码认证，申请令牌，携带令牌查询用户信息，保存用户信息到数据库
     * @param code
     * @return
     */
    public XcUser wxAuth(String code);
}
