package com.panpuntos.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Canje {

    private int idCanje;
    private Cliente cliente;
    private Recompensa recompensa;
    private LocalDateTime fecha;
    private int puntosUsados;

    public Canje(Cliente cliente, Recompensa recompensa) {
        this.cliente = cliente;
        this.recompensa = recompensa;
        this.puntosUsados = recompensa.getPuntosNecesarios();
    }
}
