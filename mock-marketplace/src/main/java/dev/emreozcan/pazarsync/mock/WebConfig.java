package dev.emreozcan.pazarsync.mock;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FaultInterceptor faultInterceptor;

    public WebConfig(FaultInterceptor faultInterceptor) {
        this.faultInterceptor = faultInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(faultInterceptor).addPathPatterns("/suppliers/*/orders", "/suppliers/*/questions");
    }
}
