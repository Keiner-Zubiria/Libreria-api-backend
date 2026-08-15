package com.sena.api_libreria.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PedidoRequest {

    private Long usuarioId;

    private String telefono;
    private String direccion;
    private String ciudad;

    @NotBlank(message = "Selecciona un método de pago.")
    private String metodoPago;

    // Datos de tarjeta (obligatorios si el método de pago es Tarjeta).
    private String titularTarjeta;
    private String numeroTarjeta;
    private String vencimientoTarjeta;
    private String cvvTarjeta;

    // Banco seleccionado (obligatorio si el método de pago es PSE).
    private String banco;

    private Double total;

    @NotNull(message = "El pedido no tiene productos.")
    @NotEmpty(message = "El pedido no tiene productos.")
    @Valid
    private List<ProductoPedidoRequest> productos;

    public PedidoRequest() {
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getTitularTarjeta() {
        return titularTarjeta;
    }

    public void setTitularTarjeta(String titularTarjeta) {
        this.titularTarjeta = titularTarjeta;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getVencimientoTarjeta() {
        return vencimientoTarjeta;
    }

    public void setVencimientoTarjeta(String vencimientoTarjeta) {
        this.vencimientoTarjeta = vencimientoTarjeta;
    }

    public String getCvvTarjeta() {
        return cvvTarjeta;
    }

    public void setCvvTarjeta(String cvvTarjeta) {
        this.cvvTarjeta = cvvTarjeta;
    }

    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public List<ProductoPedidoRequest> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoPedidoRequest> productos) {
        this.productos = productos;
    }
}
