package com.sena.api_libreria.controller;

import com.sena.api_libreria.dto.DetallePedidoResponse;
import com.sena.api_libreria.dto.PedidoRequest;
import com.sena.api_libreria.dto.PedidoResponse;
import com.sena.api_libreria.dto.ProductoPedidoRequest;
import com.sena.api_libreria.model.DetallePedido;
import com.sena.api_libreria.model.Libro;
import com.sena.api_libreria.model.Pedido;
import com.sena.api_libreria.model.Usuario;
import com.sena.api_libreria.repository.DetallePedidoRepository;
import com.sena.api_libreria.repository.LibroRepository;
import com.sena.api_libreria.repository.PedidoRepository;
import com.sena.api_libreria.repository.UsuarioRepository;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;

    public PedidoController(
            PedidoRepository pedidoRepository,
            DetallePedidoRepository detallePedidoRepository,
            UsuarioRepository usuarioRepository,
            LibroRepository libroRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
    }

    // Crea un nuevo pedido.
    @PostMapping
    @Transactional
    public ResponseEntity<?> crearPedido(
            @Valid @RequestBody PedidoRequest request
    ) {

        // Usa el usuario autenticado con el token.
        Authentication autenticacion =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        Usuario usuario =
                (Usuario) autenticacion.getPrincipal();

        // Simula el cobro según el método de pago elegido.
        String resultadoPago =
                simularPago(request);

        if (!"Aprobado".equals(resultadoPago)) {

            return ResponseEntity
                    .badRequest()
                    .body(resultadoPago);
        }

        Pedido pedido = new Pedido();

        pedido.setUsuario(usuario);
        pedido.setFecha(LocalDateTime.now(ZoneId.of("America/Bogota")));
        pedido.setEstado("Pendiente");
        pedido.setMetodoPago(request.getMetodoPago());

        // Contraentrega se paga al recibir; el resto queda pagado.
        if ("Contraentrega".equalsIgnoreCase(request.getMetodoPago())) {
            pedido.setEstadoPago("Pendiente");
        } else {
            pedido.setEstadoPago("Pagado");
        }

        pedido.setTelefono(request.getTelefono());
        pedido.setDireccion(request.getDireccion());
        pedido.setCiudad(request.getCiudad());

        double total = 0;

        for (ProductoPedidoRequest producto : request.getProductos()) {

            Libro libro = libroRepository
                    .findById(producto.getId())
                    .orElse(null);

            if (libro == null) {

                return ResponseEntity
                        .badRequest()
                        .body("Libro no encontrado: " + producto.getId());
            }

            String formatoNormalizado = normalizarFormato(producto.getFormato());

            if ("Fisico".equalsIgnoreCase(formatoNormalizado)) {

                if (libro.getStock() < producto.getQuantity()) {

                    return ResponseEntity
                            .badRequest()
                            .body("Stock insuficiente para: " + libro.getTitulo());
                }

                libro.setStock(
                        libro.getStock() - producto.getQuantity()
                );

                libro.setVendidos(
                        (libro.getVendidos() == null ? 0 : libro.getVendidos())
                                + producto.getQuantity()
                );

                libroRepository.save(libro);

                total +=
                        libro.getPrecioFisico()
                                * producto.getQuantity();

            } else {

                total +=
                        libro.getPrecioVirtual()
                                * producto.getQuantity();
            }
        }

        pedido.setTotal(total);

        // Un pedido 100% virtual y pagado en línea se entrega al instante
        // (la descarga del PDF). Los demás quedan pendientes de envío.
        boolean todosVirtuales = request.getProductos()
                .stream()
                .allMatch(producto ->
                        "Virtual".equalsIgnoreCase(
                                normalizarFormato(producto.getFormato()))
                );

        boolean pagoEnLinea =
                !"Contraentrega".equalsIgnoreCase(request.getMetodoPago());

        if (todosVirtuales && pagoEnLinea) {

            pedido.setEstado("Entregado");
        }

        Pedido pedidoGuardado =
                pedidoRepository.save(pedido);
        for (ProductoPedidoRequest producto : request.getProductos()) {

            Libro libro = libroRepository
                    .findById(producto.getId())
                    .orElse(null);

            if (libro == null) {
                continue;
            }

            DetallePedido detalle = new DetallePedido();

            String formatoNorm = normalizarFormato(producto.getFormato());

            detalle.setPedido(pedidoGuardado);
            detalle.setLibro(libro);
            detalle.setCantidad(producto.getQuantity());
            detalle.setFormato(formatoNorm);

            if ("Fisico".equalsIgnoreCase(formatoNorm)) {

                detalle.setPrecio(libro.getPrecioFisico());

            } else {

                detalle.setPrecio(libro.getPrecioVirtual());
            }

            detallePedidoRepository.save(detalle);

            // Agrega el detalle a la colección para que la respuesta
            // incluya los productos comprados en el mismo momento.
            pedidoGuardado.getDetalles().add(detalle);
        }

        return ResponseEntity.ok(
                convertirPedido(pedidoGuardado)
        );
    }

    // Quita tildes y convierte a minúsculas para comparar formatos.
    private String normalizarFormato(String formato) {

        if (formato == null) return "";

        String normalizado = Normalizer
                .normalize(formato, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .trim()
                .toLowerCase();

        if (normalizado.equals("fisico")) return "Fisico";
        if (normalizado.equals("virtual")) return "Virtual";

        return formato;
    }

    // Simula el procesamiento de un pago y devuelve el resultado.
    private String simularPago(PedidoRequest request) {

        String metodo = request.getMetodoPago();

        if ("Contraentrega".equalsIgnoreCase(metodo)) {
            return "Aprobado";
        }

        if ("Tarjeta".equalsIgnoreCase(metodo)) {

            // Simula un rechazo cuando la tarjeta es inválida.
            String numero = request.getNumeroTarjeta();

            if (numero == null ||
                    numero.replaceAll("\\s", "").length() < 13) {

                return "El número de tarjeta no es válido.";
            }

            if (numero.replaceAll("\\s", "").endsWith("0000")) {

                return "El pago fue rechazado. Verifica los datos de la tarjeta.";
            }

            return "Aprobado";
        }

        if ("PSE".equalsIgnoreCase(metodo)) {

            if (request.getBanco() == null ||
                    request.getBanco().isBlank()) {

                return "Selecciona un banco para continuar.";
            }

            return "Aprobado";
        }

        return "El método de pago no es válido.";
    }

    // Lista todos los pedidos.
    @GetMapping
    public ResponseEntity<?> listarPedidos() {

        var respuesta = pedidoRepository.findAll()
                .stream()
                .map(this::convertirPedido)
                .toList();

        return ResponseEntity.ok(respuesta);
    }

    // Lista los pedidos de un usuario.
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> listarPedidosUsuario(
            @PathVariable Long usuarioId
    ) {

        Optional<Usuario> usuario =
                usuarioRepository.findById(usuarioId);

        if (usuario.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Usuario no encontrado");
        }

        // Solo el propio usuario o un administrador pueden verlos.
        Authentication autenticacion =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        Usuario autenticado =
                (Usuario) autenticacion.getPrincipal();

        if (!autenticado.getId().equals(usuarioId) &&
                !"administrador".equalsIgnoreCase(autenticado.getRol())) {

            return ResponseEntity
                    .status(403)
                    .body("No tienes permiso para ver estos pedidos.");
        }

        var respuesta = pedidoRepository
                .findByUsuarioId(usuarioId)
                .stream()
                .map(this::convertirPedido)
                .toList();

        return ResponseEntity.ok(respuesta);
    }

    // Busca un pedido por id.
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPedido(
            @PathVariable Long id
    ) {

        Optional<Pedido> pedido =
                pedidoRepository.findById(id);

        if (pedido.isEmpty()) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        // Solo el dueño del pedido o un administrador pueden verlo.
        Authentication autenticacion =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        Usuario autenticado =
                (Usuario) autenticacion.getPrincipal();

        boolean esAdministrador =
                "administrador".equalsIgnoreCase(autenticado.getRol());

        if (!autenticado.getId().equals(pedido.get().getUsuario().getId()) &&
                !esAdministrador) {

            return ResponseEntity
                    .status(403)
                    .body("No tienes permiso para ver este pedido.");
        }

        return ResponseEntity.ok(
                convertirPedido(pedido.get())
        );
    }

    // Actualiza el estado del pedido.
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestBody String estado
    ) {

        Optional<Pedido> pedidoOpt =
                pedidoRepository.findById(id);

        if (pedidoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Pedido pedido = pedidoOpt.get();

        pedido.setEstado(
                estado.replace("\"", "")
        );

        pedidoRepository.save(pedido);

        return ResponseEntity.ok(
                convertirPedido(pedido)
        );
    }

    // Elimina un pedido.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPedido(
            @PathVariable Long id
    ) {

        if (!pedidoRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        pedidoRepository.deleteById(id);

        return ResponseEntity.ok(
                "Pedido eliminado correctamente."
        );
    }

    // Convierte un pedido a DTO.
    private PedidoResponse convertirPedido(Pedido pedido) {

        PedidoResponse dto = new PedidoResponse();

        dto.setId(pedido.getId());
        dto.setFecha(pedido.getFecha());
        dto.setEstado(pedido.getEstado());
        dto.setEstadoPago(pedido.getEstadoPago());
        dto.setMetodoPago(pedido.getMetodoPago());
        dto.setTotal(pedido.getTotal());

        dto.setTelefono(pedido.getTelefono());
        dto.setDireccion(pedido.getDireccion());
        dto.setCiudad(pedido.getCiudad());

        dto.setNombreUsuario(
                pedido.getUsuario().getNombre()
        );

        dto.setCorreoUsuario(
                pedido.getUsuario().getCorreo()
        );

        var productos = pedido.getDetalles().stream().map(detalle -> {

            DetallePedidoResponse detalleDto =
                    new DetallePedidoResponse();

            detalleDto.setId(
                    detalle.getLibro().getId()
            );

            detalleDto.setTitulo(
                    detalle.getLibro().getTitulo()
            );

            detalleDto.setAutor(
                    detalle.getLibro().getAutor()
            );

            detalleDto.setImagen(
                    detalle.getLibro().getImagen()
            );

            detalleDto.setFormato(
                    detalle.getFormato()
            );

            detalleDto.setCantidad(
                    detalle.getCantidad()
            );

            detalleDto.setPrecio(
                    detalle.getPrecio()
            );

            // Indica si el libro tiene PDF subido para poder descargarlo.
            String archivo =
                    detalle.getLibro().getArchivo();

            detalleDto.setTieneArchivo(
                    archivo != null && !archivo.isBlank()
            );

            return detalleDto;

        }).toList();

        dto.setProductos(productos);

        return dto;
    }
}