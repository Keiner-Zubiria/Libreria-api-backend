package com.sena.api_libreria;

import com.sena.api_libreria.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "ClaveSecretaDePruebaParaJWT12345678901234",
            3600000
    );

    @Test
    void generaTokenValido() {
        String token = jwtService.generarToken(1L, "usuario");
        assertNotNull(token);
        assertTrue(token.length() > 20);
    }

    @Test
    void extraeUsuarioIdDelToken() {
        String token = jwtService.generarToken(42L, "administrador");
        Long id = jwtService.extraerUsuarioId(token);
        assertEquals(42L, id);
    }

    @Test
    void tokenRecienCreadoEsValido() {
        String token = jwtService.generarToken(1L, "usuario");
        assertTrue(jwtService.esValido(token));
    }

    @Test
    void tokenFalsoNoEsValido() {
        assertFalse(jwtService.esValido("token.inexistente.abc123"));
    }
}
