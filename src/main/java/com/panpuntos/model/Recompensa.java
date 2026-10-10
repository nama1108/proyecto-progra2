package com.panpuntos.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Recompensa {

    private int idRecompensa;
    private String nombre;
    private int puntosNecesarios;
    private Producto producto;
    private Menu menu;
    private boolean activo;

    public Recompensa() {
        this.activo = true;
    }

    public String getTipo() {
        if (producto != null) {
            return "PRODUCTO";
        }
        return "MENU";
    }

    public List<Producto> getProductosInventario() {
        if (producto != null) {
            List<Producto> productos = new ArrayList<>();
            productos.add(producto);
            return productos;
        }
        return menu.getComponentes();
    }

    public boolean estaDisponible() {
        if (!activo) {
            return false;
        }
        if (producto != null) {
            return producto.estaDisponible(1);
        }
        return menu.estaDisponible(1);
    }

    @Override
    public String toString() {
        return nombre + " (" + puntosNecesarios + " pts)";
    }
}
