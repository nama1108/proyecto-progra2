package com.panpuntos.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuCompleto extends ItemVenta {

    public static final int PUNTOS_POR_MENU = 8;

    private Menu menu;

    public MenuCompleto(Menu menu, int cantidad) {
        super(cantidad, menu.getPrecio());
        this.menu = menu;
    }

    @Override
    public int calcularPuntos() {
        return getCantidad() * PUNTOS_POR_MENU;
    }

    @Override
    public String getNombre() {
        return menu.getNombre();
    }

    @Override
    public String getTipo() {
        return "MENU";
    }

    @Override
    public List<Producto> getProductosInventario() {
        return menu.getComponentes();
    }

    @Override
    public boolean estaDisponible() {
        return menu.estaDisponible(getCantidad());
    }
}
