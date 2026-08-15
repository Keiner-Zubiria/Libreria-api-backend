package com.sena.api_libreria.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
// Servicio encargado de generar y validar los tokens JWT.
public class JwtService {

    private final SecretKey clave;
    private final long expiracion;

    // Crea la clave de firma a partir del secreto y define la duración del token.
    public JwtService(
            @Value("${jwt.secret}") String secreto,
            @Value("${jwt.expiration}") long expiracion) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracion = expiracion;
    }

    // Crea un token con el id del usuario y su rol como datos.
    public String generarToken(Long usuarioId, String rol) {
        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiracion))
                .signWith(clave)
                .compact();
    }

    // Obtiene el id del usuario contenido en el token.
    public Long extraerUsuarioId(String token) {
        return Long.parseLong(obtenerClaims(token).getSubject());
    }

    // Verifica que el token sea válido y no haya expirado.
    public boolean esValido(String token) {
        try {
            return obtenerClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Lee la información firmada dentro del token.
    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
