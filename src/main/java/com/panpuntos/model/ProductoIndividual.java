package com.panpuntos.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoIndividual extends ItemVenta {

    public static final int PUNTOS_POR_SANDWICH = 2;

    private Producto producto;

    public ProductoIndividual(Producto producto, int cantidad) {
        super(cantidad, producto.getPrecio());
        this.producto = producto;
    }

    @Override
    public int calcularPuntos() {
        if (producto.esSandwich()) {
            return getCantidad() * PUNTOS_POR_SANDWICH;
        }
        return 0;
    }

    @Override
    public String getNombre() {
        return producto.getNombre();
    }

    @Override
    public String getTipo() {
        return "PRODUCTO";
    }

    @Override
    public List<Producto> getProductosInventario() {
        List<Producto> productos = new ArrayList<>();
        productos.add(producto);
        return productos;
    }

    @Override
    public boolean estaDisponible() {
        return producto.estaDisponible(getCantidad());
    }
}
