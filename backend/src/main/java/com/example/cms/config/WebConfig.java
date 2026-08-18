package com.example.cms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 全局配置：统一接口版本前缀
 *
 * 所有标注 @RestController 的控制器自动加上 /v1 前缀，
 * 配合 context-path=/api，最终接口路径为 /api/v1/xxx。
 *
 * 好处：不用在每个 Controller 的 @RequestMapping 里手写版本号，
 * 未来出 v2 时新增控制器或调整前缀判断即可，两套版本可并存。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/v1",
                HandlerTypePredicate.forAnnotation(RestController.class));
    }
}
