package com.luna.baidu.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.luna.baidu.api.BaiduApiConstant;
import com.luna.common.date.DateUtil;
import com.luna.common.dto.constant.ResultCode;
import com.luna.common.exception.BaseException;
import com.luna.common.net.HttpUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Luna@win10
 * @date 2020/4/26 21:44
 */

@EnableScheduling
@Component
public class BaiduKeyGenerate {
    private static final Logger log = LoggerFactory.getLogger(BaiduKeyGenerate.class);

    private BaiduProperties     baiduProperties;

    public BaiduKeyGenerate(BaiduProperties baiduProperties) {
        this.baiduProperties = baiduProperties;
    }

    public BaiduKeyGenerate() {}

    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     * 
     * @return assess_token 示例：
     * "25.2634aa914e737196878361a42128d998.315360000.1910247307.282335-19618961"
     */
    @Scheduled(cron = "0 0 0 1,15 * ? ")
    public void getAuth() {
        String ak = baiduProperties.getAppKey();
        String sk = baiduProperties.getSecretKey();
        try {
            HttpResponse httpResponse = HttpUtils.doGet(BaiduApiConstant.HOST, BaiduApiConstant.API_KEY, null,
                ImmutableMap.of("grant_type", "client_credentials", "client_id", ak, "client_secret", sk));
            String result = HttpUtils.checkResponseAndGetResult(httpResponse);
            JSONObject jsonObject = JSON.parseObject(result);
            baiduProperties.setBaiduKey(jsonObject.get("access_token").toString());
            log.info(DateUtil.getNowDate() + "执行定时任务获取百度Key: \n" + jsonObject.get("access_token").toString());
        } catch (Exception e) {
            throw new BaseException(ResultCode.ERROR_SYSTEM_EXCEPTION, e.getMessage());
        }
    }

}
