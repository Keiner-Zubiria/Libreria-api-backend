package com.sena.api_libreria.config;

import com.sena.api_libreria.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
// Configuración de seguridad: define qué rutas están protegidas.
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    // Crea la cadena de filtros con las reglas de acceso a la API.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas: registro, login, recuperación de contraseña e imágenes.
                        .requestMatchers(
                                "/usuarios/registro",
                                "/usuarios/login",
                                "/usuarios/recuperar",
                                "/usuarios/restablecer",
                                "/uploads/**"
                        ).permitAll()
                        // Descargar un libro digital requiere iniciar sesión.
                        .requestMatchers(HttpMethod.GET, "/libros/*/descargar").authenticated()
                        // Ver catálogo y categorías no requiere sesión.
                        .requestMatchers(HttpMethod.GET, "/libros/**", "/categorias/**").permitAll()
                        // Lista de usuarios y cambio de rol solo para administradores.
                        .requestMatchers("/usuarios/lista").hasRole("ADMINISTRADOR")
                        .requestMatchers("/usuarios/*/rol").hasRole("ADMINISTRADOR")
                        // Crear, editar y eliminar libros o categorías solo para administradores.
                        .requestMatchers(HttpMethod.POST, "/libros/**", "/categorias/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/libros/**", "/categorias/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/libros/**", "/categorias/**").hasRole("ADMINISTRADOR")
                        // Crear un pedido solo requiere estar autenticado.
                        .requestMatchers(HttpMethod.POST, "/pedidos").authenticated()
                        .requestMatchers("/pedidos/usuario/**").authenticated()
                        // Consultar un pedido por id requiere sesión (el controlador valida el dueño).
                        .requestMatchers(HttpMethod.GET, "/pedidos/*").authenticated()
                        // El resto de la gestión de pedidos es solo de administradores.
                        .requestMatchers("/pedidos/**").hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated()
                )
                // Respuestas claras cuando no hay sesión o el acceso es denegado.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.getWriter().write("No autenticado.");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.getWriter().write("Acceso denegado.");
                        })
                )
                // El filtro de JWT corre antes que la autenticación por usuario/contraseña.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    // Permite que el frontend consuma la API desde los orígenes configurados.
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Lee los orígenes permitidos desde la variable CORS_ORIGINS
        // (separados por coma). Por defecto permite el frontend local.
        String orígenes = System.getenv("CORS_ORIGINS");
        if (orígenes == null || orígenes.isBlank()) {
            orígenes = "http://localhost:5173";
        }

        config.setAllowedOrigins(
                List.of(orígenes.split(","))
        );
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        // Expone los encabezados de descarga para que el frontend lea el nombre del archivo.
        config.setExposedHeaders(List.of("Content-Disposition", "X-File-Name"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
