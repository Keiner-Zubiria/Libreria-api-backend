package com.sena.api_libreria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
// Configuración del codificador de contraseñas.
public class PasswordConfig
{
    @Bean
    // Devuelve el codificador BCrypt para cifrar las contraseñas.
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}