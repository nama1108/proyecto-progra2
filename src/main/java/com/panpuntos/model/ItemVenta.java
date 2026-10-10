package com.panpuntos.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ItemVenta {

    private int idDetalle;
    private int cantidad;
    private double precioUnitario;

    protected ItemVenta(int cantidad, double precioUnitario) {
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public abstract int calcularPuntos();

    public abstract String getNombre();

    public abstract String getTipo();

    public abstract List<Producto> getProductosInventario();

    public abstract boolean estaDisponible();

    public double getSubtotal() {
        return cantidad * precioUnitario;
    }
}
