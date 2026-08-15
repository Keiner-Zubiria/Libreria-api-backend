package com.sena.api_libreria.dto;

public class DetallePedidoResponse {

    private Long id;
    private String titulo;
    private String autor;
    private String imagen;
    private String formato;
    private Integer cantidad;
    private Double precio;
    private Boolean tieneArchivo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Boolean getTieneArchivo() {
        return tieneArchivo;
    }

    public void setTieneArchivo(Boolean tieneArchivo) {
        this.tieneArchivo = tieneArchivo;
    }
}