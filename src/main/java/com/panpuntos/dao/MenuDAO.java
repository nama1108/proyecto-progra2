package com.panpuntos.dao;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.model.Categoria;
import com.panpuntos.model.Menu;
import com.panpuntos.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    private static final String CONSULTA = "SELECT m.id_menu, m.codigo, m.nombre, m.precio, m.activo, "
            + "s.id_producto s_id, s.codigo s_codigo, s.nombre s_nombre, s.categoria s_categoria, s.precio s_precio, s.existencia s_existencia, s.activo s_activo, "
            + "b.id_producto b_id, b.codigo b_codigo, b.nombre b_nombre, b.categoria b_categoria, b.precio b_precio, b.existencia b_existencia, b.activo b_activo, "
            + "a.id_producto a_id, a.codigo a_codigo, a.nombre a_nombre, a.categoria a_categoria, a.precio a_precio, a.existencia a_existencia, a.activo a_activo "
            + "FROM menus m "
            + "JOIN productos s ON s.id_producto = m.id_sandwich "
            + "JOIN productos b ON b.id_producto = m.id_bebida "
            + "JOIN productos a ON a.id_producto = m.id_acompanamiento ";

    public List<Menu> listarActivos() throws SQLException {
        String sql = CONSULTA + "WHERE m.activo = 'S' ORDER BY m.codigo";
        List<Menu> menus = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                menus.add(mapear(rs));
            }
        }
        return menus;
    }

    public Menu buscarPorId(Connection con, int idMenu) throws SQLException {
        String sql = CONSULTA + "WHERE m.id_menu = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMenu);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    private Menu mapear(ResultSet rs) throws SQLException {
        Menu menu = new Menu();
        menu.setIdMenu(rs.getInt("id_menu"));
        menu.setCodigo(rs.getString("codigo"));
        menu.setNombre(rs.getString("nombre"));
        menu.setPrecio(rs.getDouble("precio"));
        menu.setActivo("S".equals(rs.getString("activo")));
        menu.setSandwich(mapearProducto(rs, "s_"));
        menu.setBebida(mapearProducto(rs, "b_"));
        menu.setAcompanamiento(mapearProducto(rs, "a_"));
        return menu;
    }

    private Producto mapearProducto(ResultSet rs, String prefijo) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt(prefijo + "id"));
        producto.setCodigo(rs.getString(prefijo + "codigo"));
        producto.setNombre(rs.getString(prefijo + "nombre"));
        producto.setCategoria(Categoria.valueOf(rs.getString(prefijo + "categoria")));
        producto.setPrecio(rs.getDouble(prefijo + "precio"));
        producto.setExistencia(rs.getInt(prefijo + "existencia"));
        producto.setActivo("S".equals(rs.getString(prefijo + "activo")));
        return producto;
    }
}
