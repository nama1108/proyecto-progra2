package com.panpuntos.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cliente {

    private int idCliente;
    private String tipoIdentificacion;
    private String identificacion;
    private String nombre;
    private String telefono;
    private String correo;
    private int puntos;
    private boolean activo;
    private LocalDateTime fechaRegistro;

    public Cliente() {
        this.tipoIdentificacion = "NIT";
        this.puntos = 0;
        this.activo = true;
    }

    public Cliente(String tipoIdentificacion, String identificacion, String nombre, String telefono, String correo) {
        this();
        this.tipoIdentificacion = tipoIdentificacion;
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
    }

    public boolean tienePuntosSuficientes(int puntosNecesarios) {
        return puntos >= puntosNecesarios;
    }

    @Override
    public String toString() {
        return identificacion + " - " + nombre;
    }
}
