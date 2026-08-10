package com.sena.api_libreria.controller;

import com.sena.api_libreria.model.Libro;
import com.sena.api_libreria.repository.LibroRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libros")
public class LibroController {

    private final LibroRepository libroRepository;

    public LibroController(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    @GetMapping
    public List<Libro> listarLibros() {
        return libroRepository.findAll();
    }

    @PostMapping
    public Libro guardarLibro(@RequestBody Libro libro) {
        return libroRepository.save(libro);
    }

    @GetMapping("/{id}")
    public Libro buscarPorId(@PathVariable Long id) {
        return libroRepository.findById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void eliminarLibro(@PathVariable Long id) {
        libroRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public Libro actualizarLibro(@PathVariable Long id, @RequestBody Libro libroActualizado) {

        Libro libro = libroRepository.findById(id).orElse(null);

        if (libro == null) {
            return null;
        }

        libro.setTitulo(libroActualizado.getTitulo());
        libro.setAutor(libroActualizado.getAutor());
        libro.setCategoria(libroActualizado.getCategoria());
        libro.setPrecioFisico(libroActualizado.getPrecioFisico());
        libro.setPrecioVirtual(libroActualizado.getPrecioVirtual());
        libro.setStock(libroActualizado.getStock());
        libro.setStockVirtual(libroActualizado.getStockVirtual());
        libro.setFormatos(libroActualizado.getFormatos());
        libro.setImagen(libroActualizado.getImagen());
        libro.setDescripcion(libroActualizado.getDescripcion());
        libro.setCalificacion(libroActualizado.getCalificacion());
        libro.setVendidos(libroActualizado.getVendidos());
        libro.setDestacado(libroActualizado.getDestacado());

        return libroRepository.save(libro);
    }
}
