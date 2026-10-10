package com.panpuntos.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagoTarjeta extends Pago {

    private String referencia;
    private boolean aprobadoPorBanco;

    public PagoTarjeta(int idPedido, double monto, String referencia, boolean aprobadoPorBanco) {
        super(idPedido, monto);
        this.referencia = referencia;
        this.aprobadoPorBanco = aprobadoPorBanco;
    }

    @Override
    public String getMetodo() {
        return "TARJETA";
    }

    @Override
    public void procesar() {
        if (aprobadoPorBanco) {
            setEstado(EstadoPago.APROBADO);
        } else {
            setEstado(EstadoPago.RECHAZADO);
        }
    }
}
