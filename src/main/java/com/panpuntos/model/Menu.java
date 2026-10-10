package com.panpuntos.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Menu {

    private int idMenu;
    private String codigo;
    private String nombre;
    private double precio;
    private Producto sandwich;
    private Producto bebida;
    private Producto acompanamiento;
    private boolean activo;

    public Menu() {
        this.activo = true;
    }

    public Menu(String codigo, String nombre, double precio, Producto sandwich, Producto bebida, Producto acompanamiento) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.sandwich = sandwich;
        this.bebida = bebida;
        this.acompanamiento = acompanamiento;
    }

    public List<Producto> getComponentes() {
        List<Producto> componentes = new ArrayList<>();
        componentes.add(sandwich);
        componentes.add(bebida);
        componentes.add(acompanamiento);
        return componentes;
    }

    public boolean estaDisponible(int cantidad) {
        if (!activo) {
            return false;
        }
        for (Producto componente : getComponentes()) {
            if (!componente.estaDisponible(cantidad)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
