package com.sena.api_libreria.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        String url = System.getenv("CLOUDINARY_URL");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                "La variable de entorno CLOUDINARY_URL no está configurada. "
                + "Formato: cloudinary://api_key:api_secret@cloud_name"
            );
        }
        return new Cloudinary(url);
    }
}
