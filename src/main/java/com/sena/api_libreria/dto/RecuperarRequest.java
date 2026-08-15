package com.sena.api_libreria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Datos enviados por el usuario para solicitar la recuperación de contraseña.
public class RecuperarRequest {

    @NotBlank(message = "Ingresa tu correo electrónico.")
    @Email(message = "El correo no tiene un formato válido.")
    private String correo;

    public RecuperarRequest() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
