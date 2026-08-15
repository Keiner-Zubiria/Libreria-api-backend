package com.sena.api_libreria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Datos enviados para restablecer la contraseña con el código de recuperación.
public class RestablecerRequest {

    @NotBlank(message = "Ingresa tu correo electrónico.")
    @Email(message = "El correo no tiene un formato válido.")
    private String correo;

    @NotBlank(message = "Ingresa el código de recuperación.")
    private String codigo;

    @NotBlank(message = "Ingresa la nueva contraseña.")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres.")
    private String nuevaPassword;

    public RestablecerRequest() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }
}
