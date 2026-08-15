package com.sena.api_libreria.security;

import com.sena.api_libreria.model.Usuario;
import com.sena.api_libreria.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
// Filtro que valida el token JWT en cada petición y autentica al usuario.
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    // Se ejecuta en cada petición antes de llegar al controlador.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String cabecera = request.getHeader("Authorization");

        // Solo procesa el token si la petición lo incluye.
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            String token = cabecera.substring(7);

            if (jwtService.esValido(token)) {
                Long usuarioId = jwtService.extraerUsuarioId(token);
                Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

                // Si el usuario existe, se crea la autenticación con su rol.
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();

                    var autoridad = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().toUpperCase());
                    var autenticacion = new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            List.of(autoridad)
                    );

                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
