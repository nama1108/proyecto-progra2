package com.panpuntos.dao;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.model.MovimientoPuntos;
import com.panpuntos.model.TipoMovimientoPuntos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MovimientoPuntosDAO {

    public void insertar(Connection con, MovimientoPuntos movimiento) throws SQLException {
        String sql = "INSERT INTO movimientos_puntos (id_cliente, tipo, puntos, id_pedido, id_canje) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movimiento.getIdCliente());
            ps.setString(2, movimiento.getTipo().name());
            ps.setInt(3, movimiento.getPuntos());
            if (movimiento.getIdPedido() != null) {
                ps.setInt(4, movimiento.getIdPedido());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (movimiento.getIdCanje() != null) {
                ps.setInt(5, movimiento.getIdCanje());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
        }
    }

    public List<MovimientoPuntos> listarPorCliente(int idCliente) throws SQLException {
        String sql = "SELECT id_movimiento, id_cliente, fecha, tipo, puntos, id_pedido, id_canje "
                + "FROM movimientos_puntos WHERE id_cliente = ? ORDER BY fecha DESC, id_movimiento DESC";
        List<MovimientoPuntos> movimientos = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovimientoPuntos m = new MovimientoPuntos();
                    m.setIdMovimiento(rs.getInt("id_movimiento"));
                    m.setIdCliente(rs.getInt("id_cliente"));
                    m.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    m.setTipo(TipoMovimientoPuntos.valueOf(rs.getString("tipo")));
                    m.setPuntos(rs.getInt("puntos"));
                    int idPedido = rs.getInt("id_pedido");
                    m.setIdPedido(rs.wasNull() ? null : idPedido);
                    int idCanje = rs.getInt("id_canje");
                    m.setIdCanje(rs.wasNull() ? null : idCanje);
                    movimientos.add(m);
                }
            }
        }
        return movimientos;
    }
}
