package com.panpuntos.model;

import com.panpuntos.excepcion.ValidacionException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pedido {

    private int idPedido;
    private Cliente cliente;
    private LocalDateTime fecha;
    private EstadoPedido estado;
    private ArrayList<ItemVenta> items;

    public Pedido() {
        this.estado = EstadoPedido.PENDIENTE;
        this.items = new ArrayList<>();
    }

    public Pedido(Cliente cliente) {
        this();
        this.cliente = cliente;
    }

    public void agregarItem(ItemVenta item) {
        items.add(item);
    }

    public void eliminarItem(int indice) {
        items.remove(indice);
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public double getTotal() {
        double total = 0;
        for (ItemVenta item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public int getPuntos() {
        int puntos = 0;
        for (ItemVenta item : items) {
            puntos += item.calcularPuntos();
        }
        return puntos;
    }

    public void cambiarEstado(EstadoPedido nuevoEstado) throws ValidacionException {
        if (!estado.puedeCambiarA(nuevoEstado)) {
            throw new ValidacionException("Un pedido " + estado + " no puede pasar a " + nuevoEstado);
        }
        this.estado = nuevoEstado;
    }
}
