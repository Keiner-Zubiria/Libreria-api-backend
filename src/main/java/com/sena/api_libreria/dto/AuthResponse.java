package com.sena.api_libreria.dto;

// Respuesta del login: devuelve el token y los datos del usuario.
public class AuthResponse {

    private String token;
    private UsuarioResponse usuario;

    public AuthResponse() {
    }

    public AuthResponse(String token, UsuarioResponse usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResponse usuario) {
        this.usuario = usuario;
    }
}
