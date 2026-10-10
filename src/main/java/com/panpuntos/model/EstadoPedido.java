package com.panpuntos.model;

public enum EstadoPedido {
    PENDIENTE,
    PAGADO,
    ENTREGADO,
    ANULADO;

    public boolean puedeCambiarA(EstadoPedido nuevo) {
        return switch (this) {
            case PENDIENTE -> nuevo == PAGADO || nuevo == ANULADO;
            case PAGADO -> nuevo == ENTREGADO;
            default -> false;
        };
    }
}
