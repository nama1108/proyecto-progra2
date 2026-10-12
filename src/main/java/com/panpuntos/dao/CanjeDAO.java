package com.panpuntos.dao;

import com.panpuntos.model.Canje;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CanjeDAO {

    public void insertar(Connection con, Canje canje) throws SQLException {
        String sql = "INSERT INTO canjes (id_cliente, id_recompensa, puntos_usados) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"id_canje"})) {
            ps.setInt(1, canje.getCliente().getIdCliente());
            ps.setInt(2, canje.getRecompensa().getIdRecompensa());
            ps.setInt(3, canje.getPuntosUsados());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    canje.setIdCanje(rs.getInt(1));
                }
            }
        }
    }
}
