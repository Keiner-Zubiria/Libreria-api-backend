package com.sena.api_libreria;

import com.sena.api_libreria.model.Libro;
import com.sena.api_libreria.model.Usuario;
import com.sena.api_libreria.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiLibreriaApplicationTests {

    @Test
    void modeloLibroFunciona() {
        Libro libro = new Libro();
        libro.setId(1L);
        libro.setTitulo("1984");
        libro.setAutor("George Orwell");
        libro.setStock(10);
        libro.setPrecioFisico(25000.0);

        assertEquals("1984", libro.getTitulo());
        assertEquals(10, libro.getStock());
    }

    @Test
    void modeloUsuarioFunciona() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Keiner");
        usuario.setCorreo("keiner@test.com");
        usuario.setRol("usuario");

        assertEquals("Keiner", usuario.getNombre());
        assertEquals("usuario", usuario.getRol());
    }

    @Test
    void jwtServiceGeneraToken() {
        JwtService jwtService = new JwtService(
                "ClaveSecretaDePruebaParaJWT12345678901234",
                3600000
        );

        String token = jwtService.generarToken(1L, "admin");
        assertNotNull(token);
        assertEquals(1L, jwtService.extraerUsuarioId(token));
        assertTrue(jwtService.esValido(token));
    }
}
