package com.panpuntos.dao;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.model.MovimientoInventario;
import com.panpuntos.model.Producto;
import com.panpuntos.model.TipoMovimientoInventario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovimientoInventarioDAO {

    public void insertar(Connection con, MovimientoInventario movimiento) throws SQLException {
        String sql = "INSERT INTO movimientos_inventario (id_producto, tipo, cantidad, existencia_anterior, existencia_nueva, referencia) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movimiento.getProducto().getIdProducto());
            ps.setString(2, movimiento.getTipo().name());
            ps.setInt(3, movimiento.getCantidad());
            ps.setInt(4, movimiento.getExistenciaAnterior());
            ps.setInt(5, movimiento.getExistenciaNueva());
            ps.setString(6, movimiento.getReferencia());
            ps.executeUpdate();
        }
    }

    public List<MovimientoInventario> listar(Integer idProducto) throws SQLException {
        String sql = "SELECT m.id_movimiento, m.fecha, m.tipo, m.cantidad, m.existencia_anterior, m.existencia_nueva, "
                + "m.referencia, p.id_producto, p.codigo, p.nombre "
                + "FROM movimientos_inventario m JOIN productos p ON p.id_producto = m.id_producto "
                + (idProducto != null ? "WHERE m.id_producto = ? " : "")
                + "ORDER BY m.fecha DESC, m.id_movimiento DESC";
        List<MovimientoInventario> movimientos = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (idProducto != null) {
                ps.setInt(1, idProducto);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapear(rs));
                }
            }
        }
        return movimientos;
    }

    private MovimientoInventario mapear(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        MovimientoInventario movimiento = new MovimientoInventario(producto,
                TipoMovimientoInventario.valueOf(rs.getString("tipo")), rs.getInt("cantidad"),
                rs.getInt("existencia_anterior"), rs.getInt("existencia_nueva"), rs.getString("referencia"));
        movimiento.setIdMovimiento(rs.getInt("id_movimiento"));
        movimiento.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        return movimiento;
    }
}
