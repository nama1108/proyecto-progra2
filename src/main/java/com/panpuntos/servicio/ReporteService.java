package com.panpuntos.servicio;

import com.panpuntos.dao.ReporteDAO;
import com.panpuntos.dto.ResultadoReporte;
import com.panpuntos.excepcion.ValidacionException;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReporteService {

    public static final String[] REPORTES = {
        "01 - Ventas por período",
        "02 - Productos más vendidos",
        "03 - Productos agotados o con bajo inventario",
        "04 - Puntos acumulados por cliente",
        "05 - Historial de canjes",
        "06 - Ventas de menús completos"
    };

    private final ReporteDAO reporteDAO = new ReporteDAO();

    public ResultadoReporte generar(int indice, LocalDate desde, LocalDate hasta) throws ValidacionException, SQLException {
        return switch (indice) {
            case 0 -> {
                if (desde == null || hasta == null) {
                    throw new ValidacionException("Ingrese las fechas del período");
                }
                if (desde.isAfter(hasta)) {
                    throw new ValidacionException("La fecha inicial no puede ser mayor que la fecha final");
                }
                yield reporteDAO.ventasPorPeriodo(desde, hasta);
            }
            case 1 -> reporteDAO.productosMasVendidos();
            case 2 -> reporteDAO.bajoInventario();
            case 3 -> reporteDAO.puntosPorCliente();
            case 4 -> reporteDAO.historialCanjes();
            case 5 -> reporteDAO.ventasDeMenus();
            default -> throw new ValidacionException("Seleccione un reporte");
        };
    }
}
