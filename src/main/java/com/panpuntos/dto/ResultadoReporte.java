package com.panpuntos.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultadoReporte {

    private String titulo;
    private String[] columnas;
    private List<Object[]> filas;
    private String resumen;

    public ResultadoReporte(String titulo, String... columnas) {
        this.titulo = titulo;
        this.columnas = columnas;
        this.filas = new ArrayList<>();
        this.resumen = "";
    }

    public void agregarFila(Object... valores) {
        filas.add(valores);
    }
}
