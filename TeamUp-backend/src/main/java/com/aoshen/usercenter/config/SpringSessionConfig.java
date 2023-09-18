package com.aoshen.usercenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * spring-session配置
 *      将SameSite = Lax置为None
 *        Secure = true 解决
 *
 * 上线的时候记得注释掉这个类，或者不把他交给spring 管理！！！！！！
 * @description 解决SameSite=Lax导致前端无法携带Cookie的坑
 */
@Configuration
public class SpringSessionConfig {
    public SpringSessionConfig() {
    }

    @Bean
    public CookieSerializer httpSessionIdResolver() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        // 取消仅限同一站点设置
        cookieSerializer.setSameSite("None");
        // 虽然设置了SameSite=None，但是还要设置Secure=true，浏览器才会携带cookie
        cookieSerializer.setUseSecureCookie(true);
        return cookieSerializer;
    }
}