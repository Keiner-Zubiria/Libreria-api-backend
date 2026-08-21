package com.sena.api_libreria.repository;

import com.sena.api_libreria.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    boolean existsByLibroId(Long libroId);

}