package com.panpuntos.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MovimientoPuntos {

    private int idMovimiento;
    private int idCliente;
    private LocalDateTime fecha;
    private TipoMovimientoPuntos tipo;
    private int puntos;
    private Integer idPedido;
    private Integer idCanje;

    public String getReferencia() {
        if (idPedido != null) {
            return "Pedido #" + idPedido;
        }
        return "Canje #" + idCanje;
    }
}
