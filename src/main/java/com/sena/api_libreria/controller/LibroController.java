package com.sena.api_libreria.controller;

import com.sena.api_libreria.config.CloudinaryService;
import com.sena.api_libreria.model.Libro;
import com.sena.api_libreria.model.Usuario;
import com.sena.api_libreria.repository.DetallePedidoRepository;
import com.sena.api_libreria.repository.LibroRepository;
import com.sena.api_libreria.repository.PedidoRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/libros")
public class LibroController {

    private final LibroRepository libroRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CloudinaryService cloudinaryService;

    public LibroController(
            LibroRepository libroRepository,
            PedidoRepository pedidoRepository,
            DetallePedidoRepository detallePedidoRepository,
            CloudinaryService cloudinaryService
    ) {
        this.libroRepository = libroRepository;
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public List<Libro> listarLibros() {
        return libroRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> buscarPorId(@PathVariable Long id) {
        Optional<Libro> libro = libroRepository.findById(id);
        return libro.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Libro> guardarLibro(

            @RequestParam String titulo,
            @RequestParam String autor,
            @RequestParam String categoria,

            @RequestParam Double precioFisico,
            @RequestParam Double precioVirtual,

            @RequestParam Integer stock,
            @RequestParam Integer stockVirtual,

            @RequestParam String formatos,

            @RequestParam String descripcion,

            @RequestParam Double calificacion,

            @RequestParam Integer vendidos,

            @RequestParam Boolean destacado,

            @RequestParam(required = false) MultipartFile imagen,

            @RequestParam(required = false) MultipartFile archivo

    ) throws IOException {

        Libro libro = new Libro();

        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setCategoria(categoria);

        libro.setPrecioFisico(precioFisico);
        libro.setPrecioVirtual(precioVirtual);

        libro.setStock(stock);
        libro.setStockVirtual(stockVirtual);

        libro.setFormatos(formatos);

        libro.setDescripcion(descripcion);

        libro.setCalificacion(calificacion);

        libro.setVendidos(vendidos);

        libro.setDestacado(destacado);

        if (imagen != null && !imagen.isEmpty()) {
            libro.setImagen(cloudinaryService.subirArchivo(imagen, "libreria/imagenes"));
        }

        if (archivo != null && !archivo.isEmpty()) {
            libro.setArchivo(cloudinaryService.subirArchivo(archivo, "libreria/pdfs"));
        }

        return ResponseEntity.ok(libroRepository.save(libro));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Libro> actualizarLibro(

            @PathVariable Long id,

            @RequestParam String titulo,
            @RequestParam String autor,
            @RequestParam String categoria,

            @RequestParam Double precioFisico,
            @RequestParam Double precioVirtual,

            @RequestParam Integer stock,
            @RequestParam Integer stockVirtual,

            @RequestParam String formatos,

            @RequestParam String descripcion,

            @RequestParam Double calificacion,

            @RequestParam Integer vendidos,

            @RequestParam Boolean destacado,

            @RequestParam(required = false) MultipartFile imagen,

            @RequestParam(required = false) MultipartFile archivo

    ) throws IOException {

        Optional<Libro> libroOpt = libroRepository.findById(id);

        if (libroOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Libro libro = libroOpt.get();

        if (imagen != null && !imagen.isEmpty()) {
            if (libro.getImagen() != null && !libro.getImagen().isBlank()) {
                cloudinaryService.eliminarArchivo(libro.getImagen());
            }
            libro.setImagen(cloudinaryService.subirArchivo(imagen, "libreria/imagenes"));
        }

        if (archivo != null && !archivo.isEmpty()) {
            if (libro.getArchivo() != null && !libro.getArchivo().isBlank()) {
                cloudinaryService.eliminarArchivo(libro.getArchivo());
            }
            libro.setArchivo(cloudinaryService.subirArchivo(archivo, "libreria/pdfs"));
        }

        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setCategoria(categoria);

        libro.setPrecioFisico(precioFisico);
        libro.setPrecioVirtual(precioVirtual);

        libro.setStock(stock);
        libro.setStockVirtual(stockVirtual);

        libro.setFormatos(formatos);

        libro.setDescripcion(descripcion);

        libro.setCalificacion(calificacion);

        libro.setVendidos(vendidos);

        libro.setDestacado(destacado);

        return ResponseEntity.ok(libroRepository.save(libro));
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Libro> actualizarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {

        Optional<Libro> libroOpt = libroRepository.findById(id);

        if (libroOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Libro libro = libroOpt.get();

        int stockActual = libro.getStock() != null
                ? libro.getStock()
                : 0;

        int vendidosActuales = libro.getVendidos() != null
                ? libro.getVendidos()
                : 0;

        int nuevoStock = Math.max(0, stockActual - cantidad);

        libro.setStock(nuevoStock);
        libro.setVendidos(vendidosActuales + cantidad);

        return ResponseEntity.ok(libroRepository.save(libro));
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<?> descargarLibro(@PathVariable Long id) throws IOException {

        Optional<Libro> libroOpt = libroRepository.findById(id);

        if (libroOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Libro libro = libroOpt.get();

        if (libro.getArchivo() == null || libro.getArchivo().isBlank()) {
            return ResponseEntity.badRequest().body("Este libro no tiene version digital.");
        }

        Authentication autenticacion =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        Usuario usuario = (Usuario) autenticacion.getPrincipal();

        boolean comprado = pedidoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .filter(pedido -> !"Cancelado".equalsIgnoreCase(pedido.getEstado()))
                .filter(pedido -> "Pagado".equalsIgnoreCase(pedido.getEstadoPago()))
                .flatMap(pedido -> pedido.getDetalles().stream())
                .anyMatch(detalle -> detalle.getLibro().getId().equals(id)
                        && "Virtual".equalsIgnoreCase(
                                detalle.getFormato() != null
                                    ? Normalizer.normalize(detalle.getFormato(),
                                            Normalizer.Form.NFD)
                                            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                                    : ""));

        if (!comprado) {
            return ResponseEntity.status(403).body("Debes comprar este libro para descargarlo.");
        }

        String archivo = libro.getArchivo();

        if (archivo.startsWith("http")) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(archivo))
                        .GET()
                        .build();
                HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

                String nombreOriginal = archivo;
                int slashIdx = nombreOriginal.lastIndexOf('/');
                if (slashIdx >= 0) {
                    nombreOriginal = nombreOriginal.substring(slashIdx + 1);
                }
                int queryIdx = nombreOriginal.indexOf('?');
                if (queryIdx >= 0) {
                    nombreOriginal = nombreOriginal.substring(0, queryIdx);
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreOriginal + "\"")
                        .header("X-File-Name", nombreOriginal)
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(response.body());
            } catch (Exception e) {
                return ResponseEntity.status(502).body("Error al descargar desde Cloudinary: " + e.getMessage());
            }
        }

        return ResponseEntity.status(404).body("El archivo del libro no se encontro.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarLibro(@PathVariable Long id) {
        Optional<Libro> libroOpt = libroRepository.findById(id);

        if (libroOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Libro libro = libroOpt.get();

        if (detallePedidoRepository.existsByLibroId(id)) {
            return ResponseEntity.badRequest().body("No se puede eliminar: el libro tiene pedidos asociados.");
        }

        if (libro.getImagen() != null && !libro.getImagen().isBlank()) {
            cloudinaryService.eliminarArchivo(libro.getImagen());
        }
        if (libro.getArchivo() != null && !libro.getArchivo().isBlank()) {
            cloudinaryService.eliminarArchivo(libro.getArchivo());
        }

        libroRepository.deleteById(id);
        return ResponseEntity.ok("Libro eliminado correctamente.");
    }
}
