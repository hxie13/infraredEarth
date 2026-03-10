package cn.ac.sitp.infrared.config;

import cn.ac.sitp.infrared.security.CsrfTokenInterceptor;
import cn.ac.sitp.infrared.security.SessionAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class MyWebMvcConfig implements WebMvcConfigurer {

    private final SessionAuthInterceptor sessionAuthInterceptor;
    private final CsrfTokenInterceptor csrfTokenInterceptor;

    public MyWebMvcConfig(SessionAuthInterceptor sessionAuthInterceptor, CsrfTokenInterceptor csrfTokenInterceptor) {
        this.sessionAuthInterceptor = sessionAuthInterceptor;
        this.csrfTokenInterceptor = csrfTokenInterceptor;
    }

    @Value("${accessFile.enabled:false}")
    private boolean accessFileEnabled;

    @Value("${accessFile.resourceHandler:/files/**}")
    private String resourceHandler;

    @Value("${accessFile.accessFilePath:}")
    private String accessFilePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!accessFileEnabled || accessFilePath == null || accessFilePath.isBlank()) {
            return;
        }
        String resourceLocation = Path.of(accessFilePath).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(resourceHandler).addResourceLocations(resourceLocation);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(csrfTokenInterceptor)
                .addPathPatterns("/rest/**");

        registry.addInterceptor(sessionAuthInterceptor)
                .addPathPatterns(
                        "/rest/log/**",
                        "/rest/job/list",
                        "/rest/job/add",
                        "/rest/nc/add",
                        "/rest/account/logout",
                        "/rest/account/password"
                );
    }
}
