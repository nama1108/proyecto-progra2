package com.panpuntos.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Pago {

    private int idPago;
    private int idPedido;
    private double monto;
    private EstadoPago estado;
    private LocalDateTime fecha;

    protected Pago(int idPedido, double monto) {
        this.idPedido = idPedido;
        this.monto = monto;
    }

    public abstract String getMetodo();

    public abstract void procesar();

    public boolean esAprobado() {
        return estado == EstadoPago.APROBADO;
    }
}
