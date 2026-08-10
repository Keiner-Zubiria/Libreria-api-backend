package com.sena.api_libreria.controller;

import com.sena.api_libreria.model.Usuario;
import com.sena.api_libreria.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/registro")
    public String registrar(@RequestBody Usuario usuario) {

        Optional<Usuario> existe =
                usuarioRepository.findByCorreo(usuario.getCorreo());

        if (existe.isPresent()) {
            return "Ese correo ya está registrado.";
        }

        usuarioRepository.save(usuario);

        return "Cuenta creada correctamente.";
    }

    @PostMapping("/login")
    public String login(@RequestBody Usuario datos) {

        Optional<Usuario> usuario =
                usuarioRepository.findByCorreo(datos.getCorreo());

        if (usuario.isEmpty()) {
            return "Correo o contraseña incorrectos.";
        }

        if (!usuario.get().getPassword().equals(datos.getPassword())) {
            return "Correo o contraseña incorrectos.";
        }

        return "Bienvenido " + usuario.get().getNombre();
    }
}