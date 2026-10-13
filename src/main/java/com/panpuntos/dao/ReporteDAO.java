package com.panpuntos.dao;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.dto.ResultadoReporte;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReporteDAO {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String PEDIDOS_VENDIDOS = "p.estado IN ('PAGADO', 'ENTREGADO')";

    public ResultadoReporte ventasPorPeriodo(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT p.id_pedido, pg.fecha, c.nombre, pg.metodo, pg.monto "
                + "FROM pagos pg JOIN pedidos p ON p.id_pedido = pg.id_pedido JOIN clientes c ON c.id_cliente = p.id_cliente "
                + "WHERE pg.estado = 'APROBADO' AND pg.fecha >= ? AND pg.fecha < ? ORDER BY pg.fecha";
        ResultadoReporte reporte = new ResultadoReporte("Ventas por período", "Pedido", "Fecha de pago", "Cliente", "Método", "Monto (Q)");
        double efectivo = 0;
        double tarjeta = 0;
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta.plusDays(1)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double monto = rs.getDouble("monto");
                    String metodo = rs.getString("metodo");
                    if ("EFECTIVO".equals(metodo)) {
                        efectivo += monto;
                    } else {
                        tarjeta += monto;
                    }
                    reporte.agregarFila("#" + rs.getInt("id_pedido"),
                            rs.getTimestamp("fecha").toLocalDateTime().format(FORMATO_FECHA),
                            rs.getString("nombre"), metodo, String.format("%.2f", monto));
                }
            }
        }
        reporte.setResumen(String.format("Efectivo: Q%.2f   |   Tarjeta: Q%.2f   |   TOTAL VENDIDO: Q%.2f",
                efectivo, tarjeta, efectivo + tarjeta));
        return reporte;
    }

    public ResultadoReporte productosMasVendidos() throws SQLException {
        String sql = "SELECT pr.codigo, pr.nombre, pr.categoria, SUM(x.individual) individual, SUM(x.en_menu) en_menu, "
                + "SUM(x.individual + x.en_menu) total FROM ("
                + "SELECT d.id_producto id, d.cantidad individual, 0 en_menu FROM detalle_pedido d "
                + "JOIN pedidos p ON p.id_pedido = d.id_pedido WHERE d.tipo_item = 'PRODUCTO' AND " + PEDIDOS_VENDIDOS
                + " UNION ALL SELECT m.id_sandwich, 0, d.cantidad FROM detalle_pedido d "
                + "JOIN pedidos p ON p.id_pedido = d.id_pedido JOIN menus m ON m.id_menu = d.id_menu WHERE " + PEDIDOS_VENDIDOS
                + " UNION ALL SELECT m.id_bebida, 0, d.cantidad FROM detalle_pedido d "
                + "JOIN pedidos p ON p.id_pedido = d.id_pedido JOIN menus m ON m.id_menu = d.id_menu WHERE " + PEDIDOS_VENDIDOS
                + " UNION ALL SELECT m.id_acompanamiento, 0, d.cantidad FROM detalle_pedido d "
                + "JOIN pedidos p ON p.id_pedido = d.id_pedido JOIN menus m ON m.id_menu = d.id_menu WHERE " + PEDIDOS_VENDIDOS
                + ") x JOIN productos pr ON pr.id_producto = x.id "
                + "GROUP BY pr.codigo, pr.nombre, pr.categoria ORDER BY total DESC, pr.nombre";
        ResultadoReporte reporte = new ResultadoReporte("Productos más vendidos",
                "Código", "Producto", "Categoría", "Vendido individual", "Vendido en menús", "Total unidades");
        int total = 0;
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                total += rs.getInt("total");
                reporte.agregarFila(rs.getString("codigo"), rs.getString("nombre"), rs.getString("categoria"),
                        rs.getInt("individual"), rs.getInt("en_menu"), rs.getInt("total"));
            }
        }
        reporte.setResumen("Unidades vendidas en total: " + total + "   (pedidos PAGADOS y ENTREGADOS)");
        return reporte;
    }

    public ResultadoReporte bajoInventario() throws SQLException {
        String sql = "SELECT codigo, nombre, categoria, existencia, activo FROM productos "
                + "WHERE existencia <= 5 ORDER BY existencia, nombre";
        ResultadoReporte reporte = new ResultadoReporte("Productos agotados o con bajo inventario",
                "Código", "Producto", "Categoría", "Existencia", "Situación", "Estado");
        int agotados = 0;
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int existencia = rs.getInt("existencia");
                if (existencia == 0) {
                    agotados++;
                }
                reporte.agregarFila(rs.getString("codigo"), rs.getString("nombre"), rs.getString("categoria"), existencia,
                        existencia == 0 ? "AGOTADO" : "BAJO", "S".equals(rs.getString("activo")) ? "Activo" : "Inactivo");
            }
        }
        reporte.setResumen("Productos en la lista: " + reporte.getFilas().size() + "   |   Agotados: " + agotados
                + "   (existencia menor o igual a 5)");
        return reporte;
    }

    public ResultadoReporte puntosPorCliente() throws SQLException {
        String sql = "SELECT tipo_identificacion, identificacion, nombre, puntos, activo FROM clientes ORDER BY puntos DESC, nombre";
        ResultadoReporte reporte = new ResultadoReporte("Puntos acumulados por cliente",
                "Tipo", "Identificación", "Cliente", "Saldo de puntos", "Estado");
        int total = 0;
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                total += rs.getInt("puntos");
                reporte.agregarFila(rs.getString("tipo_identificacion"), rs.getString("identificacion"), rs.getString("nombre"),
                        rs.getInt("puntos"), "S".equals(rs.getString("activo")) ? "Activo" : "Inactivo");
            }
        }
        reporte.setResumen("Clientes: " + reporte.getFilas().size() + "   |   Puntos en circulación: " + total);
        return reporte;
    }

    public ResultadoReporte historialCanjes() throws SQLException {
        String sql = "SELECT ca.id_canje, ca.fecha, c.nombre cliente, r.nombre recompensa, ca.puntos_usados "
                + "FROM canjes ca JOIN clientes c ON c.id_cliente = ca.id_cliente "
                + "JOIN recompensas r ON r.id_recompensa = ca.id_recompensa ORDER BY ca.fecha DESC, ca.id_canje DESC";
        ResultadoReporte reporte = new ResultadoReporte("Historial de canjes",
                "Canje", "Fecha", "Cliente", "Recompensa", "Puntos utilizados");
        int total = 0;
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                total += rs.getInt("puntos_usados");
                reporte.agregarFila("#" + rs.getInt("id_canje"), rs.getTimestamp("fecha").toLocalDateTime().format(FORMATO_FECHA),
                        rs.getString("cliente"), rs.getString("recompensa"), rs.getInt("puntos_usados"));
            }
        }
        reporte.setResumen("Canjes realizados: " + reporte.getFilas().size() + "   |   Puntos canjeados: " + total);
        return reporte;
    }

    public ResultadoReporte ventasDeMenus() throws SQLException {
        String sql = "SELECT m.codigo, m.nombre, SUM(d.cantidad) cantidad, SUM(d.subtotal) importe "
                + "FROM detalle_pedido d JOIN pedidos p ON p.id_pedido = d.id_pedido JOIN menus m ON m.id_menu = d.id_menu "
                + "WHERE d.tipo_item = 'MENU' AND " + PEDIDOS_VENDIDOS + " "
                + "GROUP BY m.codigo, m.nombre ORDER BY cantidad DESC, m.nombre";
        ResultadoReporte reporte = new ResultadoReporte("Ventas de menús completos",
                "Código", "Menú", "Cantidad vendida", "Importe (Q)");
        int cantidad = 0;
        double importe = 0;
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cantidad += rs.getInt("cantidad");
                importe += rs.getDouble("importe");
                reporte.agregarFila(rs.getString("codigo"), rs.getString("nombre"), rs.getInt("cantidad"),
                        String.format("%.2f", rs.getDouble("importe")));
            }
        }
        reporte.setResumen(String.format("Menús vendidos: %d   |   Importe total: Q%.2f", cantidad, importe));
        return reporte;
    }
}
