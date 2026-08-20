package com.sena.api_libreria.controller;

import com.sena.api_libreria.model.DetallePedido;
import com.sena.api_libreria.model.Libro;
import com.sena.api_libreria.model.Pedido;
import com.sena.api_libreria.model.Usuario;
import com.sena.api_libreria.repository.LibroRepository;
import com.sena.api_libreria.repository.PedidoRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/libros")
public class LibroController {

    private final LibroRepository libroRepository;
    private final PedidoRepository pedidoRepository;

    public LibroController(
            LibroRepository libroRepository,
            PedidoRepository pedidoRepository
    ) {
        this.libroRepository = libroRepository;
        this.pedidoRepository = pedidoRepository;
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

    // Sube un archivo (imagen o PDF) a la carpeta de archivos subidos.
    private String guardarArchivo(MultipartFile archivo, Path carpeta) throws IOException {

        if (!Files.exists(carpeta)) {
            Files.createDirectories(carpeta);
        }

        String nombreArchivo = UUID.randomUUID() + "_" + archivo.getOriginalFilename();

        Files.copy(
                archivo.getInputStream(),
                carpeta.resolve(nombreArchivo),
                StandardCopyOption.REPLACE_EXISTING);

        return nombreArchivo;
    }

    // Carpeta donde se guardan los archivos subidos (imágenes y PDFs).
    private Path carpetaUploads() {

        String carpeta = System.getenv("UPLOADS_DIR");
        if (carpeta == null || carpeta.isBlank()) {
            carpeta = "uploads";
        }

        return Paths.get(carpeta);
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

        Path carpeta = carpetaUploads();

        if (imagen != null && !imagen.isEmpty()) {
            libro.setImagen(guardarArchivo(imagen, carpeta));
        }

        if (archivo != null && !archivo.isEmpty()) {
            libro.setArchivo(guardarArchivo(archivo, carpeta));
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

        Path carpeta = carpetaUploads();

        if (imagen != null && !imagen.isEmpty()) {
            libro.setImagen(guardarArchivo(imagen, carpeta));
        }

        if (archivo != null && !archivo.isEmpty()) {
            libro.setArchivo(guardarArchivo(archivo, carpeta));
        }

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

    // Descarga el archivo digital solo si el usuario lo compró.
    @GetMapping("/{id}/descargar")
    public ResponseEntity<?> descargarLibro(@PathVariable Long id) throws IOException {

        Optional<Libro> libroOpt = libroRepository.findById(id);

        if (libroOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Libro libro = libroOpt.get();

        if (libro.getArchivo() == null || libro.getArchivo().isBlank()) {
            return ResponseEntity.badRequest().body("Este libro no tiene versión digital.");
        }

        Authentication autenticacion =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        Usuario usuario = (Usuario) autenticacion.getPrincipal();

        // Verifica que el usuario haya comprado una copia virtual del libro.
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

        Path rutaArchivo = carpetaUploads().resolve(libro.getArchivo());

        if (!Files.exists(rutaArchivo)) {
            return ResponseEntity.status(404).body("El archivo del libro no se encontró.");
        }

        // Usa el nombre original del archivo subido (ej. "1984.pdf"),
        // quitando el prefijo UUID con el que se guardó.
        String nombreOriginal = libro.getArchivo();
        int separador = nombreOriginal.indexOf("_");
        if (separador >= 0) {
            nombreOriginal = nombreOriginal.substring(separador + 1);
        }

        InputStream stream = Files.newInputStream(rutaArchivo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreOriginal + "\"")
                .header("X-File-Name", nombreOriginal)
                .contentType(MediaType.APPLICATION_PDF)
                .body(stream.readAllBytes());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarLibro(@PathVariable Long id) {
        if (!libroRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        libroRepository.deleteById(id);
        return ResponseEntity.ok("Libro eliminado correctamente.");
    }
}
