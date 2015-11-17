package org.borman.config;

import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
public class StaticResourceConfig extends WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter {

    /**
     * Adds a handler for external resources.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        addHandler(registry, "/js/**", "classpath:/static/js/");
        addHandler(registry, "/css/**", "classpath:/static/css/");
        addHandler(registry, "/img/**", "classpath:/static/img/");
        addHandler(registry, "/fonts/**", "classpath:/static/fonts/");
    }

    private void addHandler(ResourceHandlerRegistry registry, String handler, String locations) {
        if (!registry.hasMappingForPattern(handler)) {
            registry.addResourceHandler(handler).addResourceLocations(locations);
        }
    }

}
