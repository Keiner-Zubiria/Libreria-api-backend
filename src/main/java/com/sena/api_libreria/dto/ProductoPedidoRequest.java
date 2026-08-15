package com.sena.api_libreria.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductoPedidoRequest {

    @NotNull(message = "El libro no es válido.")
    private Long id;

    @NotNull(message = "La cantidad no es válida.")
    @Min(value = 1, message = "La cantidad debe ser al menos 1.")
    private Integer quantity;

    private Double precio;

    @NotBlank(message = "El formato del libro es obligatorio.")
    private String formato;

    public ProductoPedidoRequest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }
}
