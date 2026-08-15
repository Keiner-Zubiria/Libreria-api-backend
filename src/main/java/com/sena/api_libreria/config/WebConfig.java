package com.sena.api_libreria.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
// Configuración web para servir las imágenes subidas.
public class WebConfig implements WebMvcConfigurer {

    @Override
    // Expone la carpeta de archivos subidos (imágenes y PDFs) para que se vean en el navegador.
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String carpeta = System.getenv("UPLOADS_DIR");
        if (carpeta == null || carpeta.isBlank()) {
            carpeta = "uploads";
        }
        if (!carpeta.endsWith("/")) {
            carpeta += "/";
        }

        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + carpeta);
    }
}