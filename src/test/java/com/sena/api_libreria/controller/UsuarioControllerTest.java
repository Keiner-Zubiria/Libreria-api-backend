package com.sena.api_libreria.controller;

import com.sena.api_libreria.dto.RegistroRequest;
import com.sena.api_libreria.model.Usuario;
import com.sena.api_libreria.repository.UsuarioRepository;
import com.sena.api_libreria.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioControllerTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final UsuarioController controller = new UsuarioController(usuarioRepository, passwordEncoder, jwtService);

    @Test
    void registroPasswordDebilRechaza400() {
        RegistroRequest datos = new RegistroRequest();
        datos.setNombre("Keiner");
        datos.setCorreo("test@test.com");
        datos.setPassword("123");

        var respuesta = controller.registrar(datos);

        assertEquals(400, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().toString().contains("contraseña debe tener mínimo 8 caracteres"));
    }

    @Test
    void registroCorreoExistenteRechaza400() {
        RegistroRequest datos = new RegistroRequest();
        datos.setNombre("Keiner");
        datos.setCorreo("existente@test.com");
        datos.setPassword("Clave@1234");

        Usuario existente = new Usuario();
        existente.setCorreo("existente@test.com");
        when(usuarioRepository.findByCorreo("existente@test.com")).thenReturn(Optional.of(existente));

        var respuesta = controller.registrar(datos);

        assertEquals(400, respuesta.getStatusCode().value());
        assertEquals("Ese correo ya está registrado.", respuesta.getBody());
    }

    @Test
    void registroExitosoRetorna200() {
        RegistroRequest datos = new RegistroRequest();
        datos.setNombre("Keiner");
        datos.setCorreo("nuevo@test.com");
        datos.setPassword("Clave@1234");

        when(usuarioRepository.findByCorreo("nuevo@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Clave@1234")).thenReturn("$2a$encoded");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var respuesta = controller.registrar(datos);

        assertEquals(200, respuesta.getStatusCode().value());
        assertEquals("Cuenta creada correctamente.", respuesta.getBody());
    }

    @Test
    void loginCorreoInexistenteRechaza400() {
        Usuario datos = new Usuario();
        datos.setCorreo("noexiste@test.com");
        datos.setPassword("Clave@1234");

        when(usuarioRepository.findByCorreo("noexiste@test.com")).thenReturn(Optional.empty());

        var respuesta = controller.login(datos);

        assertEquals(400, respuesta.getStatusCode().value());
        assertEquals("Correo o contraseña incorrectos.", respuesta.getBody());
    }

    @Test
    void loginPasswordIncorrectaRechaza400() {
        Usuario datos = new Usuario();
        datos.setCorreo("test@test.com");
        datos.setPassword("Incorrecta@1");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo("test@test.com");
        usuario.setPassword("$2a$encoded");
        usuario.setRol("usuario");

        when(usuarioRepository.findByCorreo("test@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Incorrecta@1", "$2a$encoded")).thenReturn(false);

        var respuesta = controller.login(datos);

        assertEquals(400, respuesta.getStatusCode().value());
        assertEquals("Correo o contraseña incorrectos.", respuesta.getBody());
    }

    @Test
    void loginExitosoGeneraToken() {
        Usuario datos = new Usuario();
        datos.setCorreo("test@test.com");
        datos.setPassword("Clave@1234");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo("test@test.com");
        usuario.setPassword("$2a$encoded");
        usuario.setRol("usuario");

        when(usuarioRepository.findByCorreo("test@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Clave@1234", "$2a$encoded")).thenReturn(true);
        when(jwtService.generarToken(1L, "usuario")).thenReturn("token-falso-123");

        var respuesta = controller.login(datos);

        assertEquals(200, respuesta.getStatusCode().value());
    }

    @Test
    void recuperarCorreoInexistenteRechaza400() {
        com.sena.api_libreria.dto.RecuperarRequest datos = new com.sena.api_libreria.dto.RecuperarRequest();
        datos.setCorreo("noexiste@test.com");

        when(usuarioRepository.findByCorreo("noexiste@test.com")).thenReturn(Optional.empty());

        var respuesta = controller.recuperarContrasena(datos);

        assertEquals(400, respuesta.getStatusCode().value());
        assertEquals("No existe una cuenta con ese correo.", respuesta.getBody());
    }
}
