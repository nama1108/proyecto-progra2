package com.panpuntos.dao;

import com.panpuntos.model.MovimientoInventario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
