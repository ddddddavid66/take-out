package com.sky.config;

import com.sky.properties.BaiduProperties;
import com.sky.utils.BaiduUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class BaiduConfig {

    @Bean
    @ConditionalOnMissingBean
    public BaiduUtil baiduUtil(BaiduProperties baiduProperties){
        log.info("开始进行百度地图配置",baiduProperties);
        return new BaiduUtil(baiduProperties.getAK());
    }
}
