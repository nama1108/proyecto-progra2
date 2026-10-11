package com.panpuntos.dao;

import com.panpuntos.model.EstadoPago;
import com.panpuntos.model.Pago;
import com.panpuntos.model.PagoEfectivo;
import com.panpuntos.model.PagoTarjeta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class PagoDAO {

    public void insertar(Connection con, Pago pago) throws SQLException {
        String sql = "INSERT INTO pagos (id_pedido, metodo, estado, monto, monto_recibido, cambio, referencia) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"id_pago"})) {
            ps.setInt(1, pago.getIdPedido());
            ps.setString(2, pago.getMetodo());
            ps.setString(3, pago.getEstado().name());
            ps.setDouble(4, pago.getMonto());
            if (pago instanceof PagoEfectivo efectivo) {
                ps.setDouble(5, efectivo.getMontoRecibido());
                ps.setDouble(6, efectivo.getCambio());
                ps.setNull(7, Types.VARCHAR);
            } else if (pago instanceof PagoTarjeta tarjeta) {
                ps.setNull(5, Types.NUMERIC);
                ps.setNull(6, Types.NUMERIC);
                ps.setString(7, tarjeta.getReferencia());
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pago.setIdPago(rs.getInt(1));
                }
            }
        }
    }

    public Pago buscarAprobado(Connection con, int idPedido) throws SQLException {
        String sql = "SELECT id_pago, metodo, monto, monto_recibido, referencia, fecha FROM pagos "
                + "WHERE id_pedido = ? AND estado = 'APROBADO'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Pago pago;
                if ("EFECTIVO".equals(rs.getString("metodo"))) {
                    pago = new PagoEfectivo(idPedido, rs.getDouble("monto"), rs.getDouble("monto_recibido"));
                } else {
                    pago = new PagoTarjeta(idPedido, rs.getDouble("monto"), rs.getString("referencia"), true);
                }
                pago.setIdPago(rs.getInt("id_pago"));
                pago.setEstado(EstadoPago.APROBADO);
                pago.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                return pago;
            }
        }
    }
}
