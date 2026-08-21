package com.sena.api_libreria.controller;

import com.sena.api_libreria.config.CloudinaryService;
import com.sena.api_libreria.model.Libro;
import com.sena.api_libreria.repository.LibroRepository;
import com.sena.api_libreria.repository.PedidoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibroControllerTest {

    private final LibroRepository libroRepository = mock(LibroRepository.class);
    private final PedidoRepository pedidoRepository = mock(PedidoRepository.class);
    private final CloudinaryService cloudinaryService = mock(CloudinaryService.class);
    private final LibroController controller = new LibroController(libroRepository, pedidoRepository, cloudinaryService);

    private Libro crearLibro(Long id, String titulo) {
        Libro libro = new Libro();
        libro.setId(id);
        libro.setTitulo(titulo);
        libro.setAutor("Autor Test");
        libro.setPrecioFisico(25000.0);
        libro.setStock(10);
        return libro;
    }

    @Test
    void listarLibrosRetornaLista() {
        when(libroRepository.findAll()).thenReturn(List.of(
                crearLibro(1L, "1984"),
                crearLibro(2L, "Don Quijote")
        ));

        List<Libro> libros = controller.listarLibros();

        assertEquals(2, libros.size());
        assertEquals("1984", libros.get(0).getTitulo());
        assertEquals("Don Quijote", libros.get(1).getTitulo());
    }

    @Test
    void buscarPorIdExistenteRetornaLibro() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(crearLibro(1L, "1984")));

        var respuesta = controller.buscarPorId(1L);

        assertTrue(respuesta.getStatusCode().is2xxSuccessful());
        assertEquals("1984", respuesta.getBody().getTitulo());
    }

    @Test
    void buscarPorIdInexistenteRetorna404() {
        when(libroRepository.findById(999L)).thenReturn(Optional.empty());

        var respuesta = controller.buscarPorId(999L);

        assertTrue(respuesta.getStatusCode().is4xxClientError());
    }

    @Test
    void eliminarLibroExistenteRetorna200() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(crearLibro(1L, "1984")));

        var respuesta = controller.eliminarLibro(1L);

        assertTrue(respuesta.getStatusCode().is2xxSuccessful());
        verify(libroRepository).deleteById(1L);
    }

    @Test
    void eliminarLibroInexistenteRetorna404() {
        when(libroRepository.findById(999L)).thenReturn(Optional.empty());

        var respuesta = controller.eliminarLibro(999L);

        assertTrue(respuesta.getStatusCode().is4xxClientError());
        verify(libroRepository, never()).deleteById(anyLong());
    }

    @Test
    void actualizarStockDescuentaYSumaVendidos() {
        Libro libro = crearLibro(1L, "1984");
        libro.setStock(10);
        libro.setVendidos(5);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(libroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var respuesta = controller.actualizarStock(1L, 3);

        assertTrue(respuesta.getStatusCode().is2xxSuccessful());
        assertEquals(7, respuesta.getBody().getStock());
        assertEquals(8, respuesta.getBody().getVendidos());
    }

    @Test
    void actualizarStockNoBajaDeCero() {
        Libro libro = crearLibro(1L, "1984");
        libro.setStock(2);
        libro.setVendidos(0);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(libroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var respuesta = controller.actualizarStock(1L, 5);

        assertEquals(0, respuesta.getBody().getStock());
        assertEquals(5, respuesta.getBody().getVendidos());
    }
}
