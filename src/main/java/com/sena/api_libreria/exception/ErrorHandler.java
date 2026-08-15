package com.sena.api_libreria.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// Manejador central de errores de validación.
@RestControllerAdvice
public class ErrorHandler {

    // Devuelve los mensajes de las validaciones fallidas.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> manejarValidacion(MethodArgumentNotValidException ex) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(" "));

        return ResponseEntity.badRequest().body(mensaje);
    }

    // Devuelve un mensaje genérico para cualquier error inesperado.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarErrorGeneral(Exception ex) {

        ex.printStackTrace();

        return ResponseEntity
                .status(500)
                .body("Error interno del servidor.");
    }
}
