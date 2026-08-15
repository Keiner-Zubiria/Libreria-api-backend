package com.sena.api_libreria.controller;

import com.sena.api_libreria.dto.ActualizarUsuarioRequest;
import com.sena.api_libreria.dto.AuthResponse;
import com.sena.api_libreria.dto.RecuperarRequest;
import com.sena.api_libreria.dto.RegistroRequest;
import com.sena.api_libreria.dto.RestablecerRequest;
import com.sena.api_libreria.dto.UsuarioResponse;
import com.sena.api_libreria.model.Usuario;
import com.sena.api_libreria.repository.UsuarioRepository;
import com.sena.api_libreria.security.JwtService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;


// Controlador encargado de gestionar los usuarios.
@RestController
@RequestMapping("/usuarios")
public class UsuarioController
{
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService)
    {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Comprueba que la contraseña cumpla los requisitos de seguridad.
    private boolean passwordSegura(String password)
    {
        return password != null &&
                Pattern.matches(
                        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
                        password
                );
    }

    // Registra un nuevo usuario.
    @PostMapping("/registro")
    public ResponseEntity<String> registrar(
            @Valid @RequestBody RegistroRequest datos)
    {
        if (!passwordSegura(datos.getPassword()))
        {
            return ResponseEntity
                    .badRequest()
                    .body(
                            "La contraseña debe tener mínimo 8 caracteres, "
                                    + "una mayúscula, una minúscula, un número "
                                    + "y un carácter especial."
                    );
        }

        
        Optional<Usuario> existe =
                usuarioRepository.findByCorreo(
                        datos.getCorreo()
                );

        if (existe.isPresent())
        {
            return ResponseEntity
                    .badRequest()
                    .body("Ese correo ya está registrado.");
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(datos.getNombre());
        usuario.setCorreo(datos.getCorreo());

        if (datos.getRol() == null ||
                datos.getRol().isBlank())
        {
            usuario.setRol("usuario");
        }
        else
        {
            usuario.setRol(datos.getRol());
        }

        // Cifra la contraseña antes de guardarla.
        usuario.setPassword(
                passwordEncoder.encode(
                        datos.getPassword()
                )
        );

        usuarioRepository.save(usuario);

        return ResponseEntity.ok(
                "Cuenta creada correctamente."
        );
    }

    // Permite iniciar sesión con las credenciales registradas.
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Usuario datos)
    {
        Optional<Usuario> usuario =
                usuarioRepository.findByCorreo(
                        datos.getCorreo()
                );

        if (usuario.isEmpty())
        {
            return ResponseEntity
                    .badRequest()
                    .body(
                            "Correo o contraseña incorrectos."
                    );
        }

        // Compara la contraseña escrita con la contraseña cifrada.
        if (!passwordEncoder.matches(
                datos.getPassword(),
                usuario.get().getPassword()
        ))
        {
            return ResponseEntity
                    .badRequest()
                    .body(
                            "Correo o contraseña incorrectos."
                    );
        }

        String token = jwtService.generarToken(
                usuario.get().getId(),
                usuario.get().getRol()
        );

        return ResponseEntity.ok(
                new AuthResponse(
                        token,
                        new UsuarioResponse(
                                usuario.get()
                        )
                )
        );
    }

    // Genera un código temporal para recuperar la contraseña.
    @PostMapping("/recuperar")
    public ResponseEntity<?> recuperarContrasena(
            @Valid @RequestBody RecuperarRequest datos)
    {
        Optional<Usuario> usuario =
                usuarioRepository.findByCorreo(
                        datos.getCorreo().trim()
                );

        if (usuario.isEmpty())
        {
            return ResponseEntity
                    .badRequest()
                    .body("No existe una cuenta con ese correo.");
        }

        // Código de 6 dígitos válido durante 15 minutos.
        String codigo = String.format(
                "%06d",
                ThreadLocalRandom.current().nextInt(1000000)
        );

        Usuario cuenta = usuario.get();

        cuenta.setTokenRecuperacion(codigo);
        cuenta.setExpiracionToken(
                LocalDateTime.now().plusMinutes(15)
        );

        usuarioRepository.save(cuenta);

        // En un proyecto real el código se enviaría por correo.
        // Aquí se devuelve para poder probar el flujo de recuperación.
        return ResponseEntity.ok(Map.of(
                "mensaje", "Se generó un código de recuperación.",
                "codigo", codigo,
                "expiracionMinutos", 15
        ));
    }

    // Restablece la contraseña con el código de recuperación.
    @PostMapping("/restablecer")
    public ResponseEntity<?> restablecerContrasena(
            @Valid @RequestBody RestablecerRequest datos)
    {
        Optional<Usuario> usuario =
                usuarioRepository.findByCorreo(
                        datos.getCorreo().trim()
                );

        if (usuario.isEmpty())
        {
            return ResponseEntity
                    .badRequest()
                    .body("No existe una cuenta con ese correo.");
        }

        Usuario cuenta = usuario.get();

        if (cuenta.getTokenRecuperacion() == null ||
                !cuenta.getTokenRecuperacion().equals(datos.getCodigo().trim()))
        {
            return ResponseEntity
                    .badRequest()
                    .body("El código de recuperación es incorrecto.");
        }

        if (cuenta.getExpiracionToken() == null ||
                cuenta.getExpiracionToken().isBefore(LocalDateTime.now()))
        {
            return ResponseEntity
                    .badRequest()
                    .body("El código de recuperación expiró. Solicita uno nuevo.");
        }

        if (!passwordSegura(datos.getNuevaPassword()))
        {
            return ResponseEntity
                    .badRequest()
                    .body(
                            "La contraseña debe tener mínimo 8 caracteres, "
                                    + "una mayúscula, una minúscula, un número "
                                    + "y un carácter especial."
                    );
        }

        // Cifra la nueva contraseña y limpia el código usado.
        cuenta.setPassword(
                passwordEncoder.encode(
                        datos.getNuevaPassword()
                )
        );

        cuenta.setTokenRecuperacion(null);
        cuenta.setExpiracionToken(null);

        usuarioRepository.save(cuenta);

        return ResponseEntity.ok(
                "Contraseña restablecida correctamente."
        );
    }

    // Lista todos los usuarios.
    @GetMapping("/lista")
    public ResponseEntity<?> listarUsuarios()
    {
        List<UsuarioResponse> usuarios =
                usuarioRepository.findAll()
                        .stream()
                        .map(UsuarioResponse::new)
                        .toList();

        return ResponseEntity.ok(usuarios);
    }

    // Actualiza el rol de un usuario.
    @PutMapping("/{id}/rol")
    public ResponseEntity<?> actualizarRol(
            @PathVariable Long id,
            @RequestBody String rol)
    {
        Optional<Usuario> usuarioOpt =
                usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty())
        {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        String nuevoRol = rol
                .replace("\"", "")
                .trim()
                .toLowerCase();

        if (!nuevoRol.equals("usuario") &&
                !nuevoRol.equals("administrador"))
        {
            return ResponseEntity
                    .badRequest()
                    .body("Rol no válido.");
        }

        Usuario usuario = usuarioOpt.get();

        usuario.setRol(nuevoRol);

        return ResponseEntity.ok(
                new UsuarioResponse(
                        usuarioRepository.save(usuario)
                )
        );
    }

    // Actualiza los datos del perfil.
    //
    // El nombre puede cambiarse sin contraseña.
    // El correo requiere la contraseña actual.
    // La contraseña requiere la contraseña actual.
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest datos)
    {
        Optional<Usuario> usuarioOpt =
                usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty())
        {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        Usuario usuario = usuarioOpt.get();

        if (datos.getNombre() == null ||
                datos.getNombre().isBlank())
        {
            return ResponseEntity
                    .badRequest()
                    .body("El nombre es obligatorio.");
        }

        if (datos.getCorreo() == null ||
                datos.getCorreo().isBlank())
        {
            return ResponseEntity
                    .badRequest()
                    .body("El correo es obligatorio.");
        }

        boolean cambiaCorreo =
                !usuario.getCorreo()
                        .equals(datos.getCorreo().trim());

        boolean cambiaPassword =
                datos.getNuevaPassword() != null &&
                !datos.getNuevaPassword().isBlank();

        if (cambiaCorreo || cambiaPassword)
        {
            if (datos.getPasswordActual() == null ||
                    datos.getPasswordActual().isBlank())
            {
                return ResponseEntity
                        .badRequest()
                        .body(
                                "Debes ingresar tu contraseña actual."
                        );
            }

            if (!passwordEncoder.matches(
                    datos.getPasswordActual(),
                    usuario.getPassword()
            ))
            {
                return ResponseEntity
                        .badRequest()
                        .body(
                                "La contraseña actual es incorrecta."
                        );
            }
        }

        Optional<Usuario> correoExistente =
                usuarioRepository.findByCorreo(
                        datos.getCorreo().trim()
                );

        if (correoExistente.isPresent() &&
                !correoExistente.get()
                        .getId()
                        .equals(id))
        {
            return ResponseEntity
                    .badRequest()
                    .body(
                            "Ese correo ya está registrado."
                    );
        }

        usuario.setNombre(
                datos.getNombre().trim()
        );

        usuario.setCorreo(
                datos.getCorreo().trim()
        );

        if (cambiaPassword)
        {
            if (datos.getConfirmarPassword() == null ||
                    !datos.getNuevaPassword()
                            .equals(datos.getConfirmarPassword()))
            {
                return ResponseEntity
                        .badRequest()
                        .body(
                                "Las nuevas contraseñas no coinciden."
                        );
            }

            if (!passwordSegura(
                    datos.getNuevaPassword()
            ))
            {
                return ResponseEntity
                        .badRequest()
                        .body(
                                "La nueva contraseña debe tener mínimo "
                                        + "8 caracteres, una mayúscula, "
                                        + "una minúscula, un número "
                                        + "y un carácter especial."
                        );
            }

            // Cifra la nueva contraseña antes de guardarla.
            usuario.setPassword(
                    passwordEncoder.encode(
                            datos.getNuevaPassword()
                    )
            );
        }

        return ResponseEntity.ok(
                new UsuarioResponse(
                        usuarioRepository.save(usuario)
                )
        );
    }

    // Elimina la cuenta del usuario.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCuenta(
            @PathVariable Long id,
            @RequestParam String password)
    {
        Optional<Usuario> usuarioOpt =
                usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty())
        {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(
                password,
                usuario.getPassword()
        ))
        {
            return ResponseEntity
                    .badRequest()
                    .body(
                            "La contraseña actual es incorrecta."
                    );
        }

        usuarioRepository.deleteById(id);

        return ResponseEntity.ok(
                "Cuenta eliminada correctamente."
        );
    }
}