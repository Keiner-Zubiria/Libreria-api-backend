package com.sena.api_libreria.repository;

import com.sena.api_libreria.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    @Query("SELECT l FROM Libro l WHERE l.activo IS NULL OR l.activo = true")
    List<Libro> findActivos();

    @Query("SELECT l FROM Libro l WHERE l.destacado = true AND (l.activo IS NULL OR l.activo = true)")
    List<Libro> findDestacadosActivos();

}
