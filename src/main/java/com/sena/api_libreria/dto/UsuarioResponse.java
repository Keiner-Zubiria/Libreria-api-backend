package com.sena.api_libreria.dto;

import com.sena.api_libreria.model.Usuario;

// Datos del usuario que se envían al frontend, sin la contraseña.
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String correo;
    private String rol;
    private String telefono;
    private String ciudad;
    private String direccion;

    public UsuarioResponse() {
    }

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.correo = usuario.getCorreo();
        this.rol = usuario.getRol();
        this.telefono = usuario.getTelefono();
        this.ciudad = usuario.getCiudad();
        this.direccion = usuario.getDireccion();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
