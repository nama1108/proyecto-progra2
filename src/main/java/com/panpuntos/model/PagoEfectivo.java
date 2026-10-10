package com.panpuntos.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagoEfectivo extends Pago {

    private double montoRecibido;

    public PagoEfectivo(int idPedido, double monto, double montoRecibido) {
        super(idPedido, monto);
        this.montoRecibido = montoRecibido;
    }

    @Override
    public String getMetodo() {
        return "EFECTIVO";
    }

    @Override
    public void procesar() {
        if (montoRecibido >= getMonto()) {
            setEstado(EstadoPago.APROBADO);
        } else {
            setEstado(EstadoPago.RECHAZADO);
        }
    }

    public double getCambio() {
        if (esAprobado()) {
            return montoRecibido - getMonto();
        }
        return 0;
    }
}
