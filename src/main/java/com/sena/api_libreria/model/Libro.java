package com.sena.api_libreria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String autor;
    private String categoria;

    private Double precioFisico;
    private Double precioVirtual;

    private Integer stock;
    private Integer stockVirtual;

    private String formatos;

    private String imagen;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Double calificacion;

    private Integer vendidos;

    private Boolean destacado;

    public Libro() {
    }

    // Getters y Setters

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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Double getPrecioFisico() {
        return precioFisico;
    }

    public void setPrecioFisico(Double precioFisico) {
        this.precioFisico = precioFisico;
    }

    public Double getPrecioVirtual() {
        return precioVirtual;
    }

    public void setPrecioVirtual(Double precioVirtual) {
        this.precioVirtual = precioVirtual;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStockVirtual() {
        return stockVirtual;
    }

    public void setStockVirtual(Integer stockVirtual) {
        this.stockVirtual = stockVirtual;
    }

    public String getFormatos() {
        return formatos;
    }

    public void setFormatos(String formatos) {
        this.formatos = formatos;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
    }

    public Integer getVendidos() {
        return vendidos;
    }

    public void setVendidos(Integer vendidos) {
        this.vendidos = vendidos;
    }

    public Boolean getDestacado() {
        return destacado;
    }

    public void setDestacado(Boolean destacado) {
        this.destacado = destacado;
    }
}