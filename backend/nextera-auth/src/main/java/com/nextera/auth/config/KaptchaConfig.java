package com.nextera.auth.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码配置
 *
 * @author Nextera
 */
@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kaptchaProducer() {
        Properties properties = new Properties();
        
        // 图片边框
        properties.setProperty("kaptcha.border", "yes");
        properties.setProperty("kaptcha.border.color", "105,179,90");
        
        // 字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "red");
        
        // 图片宽度
        properties.setProperty("kaptcha.image.width", "110");
        
        // 图片高度
        properties.setProperty("kaptcha.image.height", "40");
        
        // 字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        
        // 字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        
        // 字体
        properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");
        
        // 字符集
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        
        // 干扰线颜色
        properties.setProperty("kaptcha.noise.color", "yellow");
        
        // 干扰实现类
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.DefaultNoise");
        
        // 背景颜色渐变，开始颜色
        properties.setProperty("kaptcha.background.clear.from", "lightGray");
        
        // 背景颜色渐变，结束颜色
        properties.setProperty("kaptcha.background.clear.to", "white");
        
        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);
        
        return defaultKaptcha;
    }
} 