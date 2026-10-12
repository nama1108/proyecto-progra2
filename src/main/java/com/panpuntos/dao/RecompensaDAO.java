package com.panpuntos.dao;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.model.Recompensa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecompensaDAO {

    private static final String COLUMNAS = "id_recompensa, nombre, puntos_necesarios, id_producto, id_menu, activo";

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MenuDAO menuDAO = new MenuDAO();

    public List<Recompensa> listarActivas() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM recompensas WHERE activo = 'S' ORDER BY puntos_necesarios";
        List<Object[]> filas = new ArrayList<>();
        List<Recompensa> recompensas = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion()) {
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(leerFila(rs));
                }
            }
            for (Object[] fila : filas) {
                recompensas.add(mapear(con, fila));
            }
        }
        return recompensas;
    }

    public Recompensa buscarPorId(Connection con, int idRecompensa) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM recompensas WHERE id_recompensa = ?";
        Object[] fila = null;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRecompensa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    fila = leerFila(rs);
                }
            }
        }
        return fila == null ? null : mapear(con, fila);
    }

    private Object[] leerFila(ResultSet rs) throws SQLException {
        return new Object[]{rs.getInt("id_recompensa"), rs.getString("nombre"), rs.getInt("puntos_necesarios"),
            rs.getInt("id_producto"), rs.getInt("id_menu"), rs.getString("activo")};
    }

    private Recompensa mapear(Connection con, Object[] fila) throws SQLException {
        Recompensa recompensa = new Recompensa();
        recompensa.setIdRecompensa((Integer) fila[0]);
        recompensa.setNombre((String) fila[1]);
        recompensa.setPuntosNecesarios((Integer) fila[2]);
        int idProducto = (Integer) fila[3];
        int idMenu = (Integer) fila[4];
        if (idProducto > 0) {
            recompensa.setProducto(productoDAO.buscarPorId(con, idProducto));
        } else {
            recompensa.setMenu(menuDAO.buscarPorId(con, idMenu));
        }
        recompensa.setActivo("S".equals(fila[5]));
        return recompensa;
    }
}
