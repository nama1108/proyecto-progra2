package com.panpuntos.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Producto {

    private int idProducto;
    private String codigo;
    private String nombre;
    private Categoria categoria;
    private double precio;
    private int existencia;
    private boolean activo;

    public Producto() {
        this.activo = true;
    }

    public Producto(String codigo, String nombre, Categoria categoria, double precio, int existencia) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.existencia = existencia;
    }

    public boolean estaDisponible(int cantidad) {
        return activo && existencia >= cantidad;
    }

    public boolean esSandwich() {
        return categoria == Categoria.SANDWICH;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
