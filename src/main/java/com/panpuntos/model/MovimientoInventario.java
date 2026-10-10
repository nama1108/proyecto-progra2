package com.panpuntos.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MovimientoInventario {

    private int idMovimiento;
    private Producto producto;
    private LocalDateTime fecha;
    private TipoMovimientoInventario tipo;
    private int cantidad;
    private int existenciaAnterior;
    private int existenciaNueva;
    private String referencia;

    public MovimientoInventario(Producto producto, TipoMovimientoInventario tipo, int cantidad,
            int existenciaAnterior, int existenciaNueva, String referencia) {
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.existenciaAnterior = existenciaAnterior;
        this.existenciaNueva = existenciaNueva;
        this.referencia = referencia;
    }
}
