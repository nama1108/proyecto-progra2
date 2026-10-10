package com.panpuntos.dao;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.model.Categoria;
import com.panpuntos.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private static final String COLUMNAS = "id_producto, codigo, nombre, categoria, precio, existencia, activo";

    public void insertar(Connection con, Producto producto) throws SQLException {
        String sql = "INSERT INTO productos (codigo, nombre, categoria, precio, existencia) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"id_producto"})) {
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getCategoria().name());
            ps.setDouble(4, producto.getPrecio());
            ps.setInt(5, producto.getExistencia());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    producto.setIdProducto(rs.getInt(1));
                }
            }
        }
    }

    public void actualizar(Producto producto) throws SQLException {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, categoria = ?, precio = ? WHERE id_producto = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getCategoria().name());
            ps.setDouble(4, producto.getPrecio());
            ps.setInt(5, producto.getIdProducto());
            ps.executeUpdate();
        }
    }

    public void actualizarExistencia(Connection con, int idProducto, int existencia) throws SQLException {
        String sql = "UPDATE productos SET existencia = ? WHERE id_producto = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, existencia);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    public void cambiarActivo(int idProducto, boolean activo) throws SQLException {
        String sql = "UPDATE productos SET activo = ? WHERE id_producto = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, activo ? "S" : "N");
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    public Producto buscarPorId(Connection con, int idProducto) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM productos WHERE id_producto = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Producto buscarParaActualizar(Connection con, int idProducto) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM productos WHERE id_producto = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Producto> listarActivos() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM productos WHERE activo = 'S' ORDER BY categoria, codigo";
        List<Producto> productos = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(mapear(rs));
            }
        }
        return productos;
    }

    public Producto buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM productos WHERE UPPER(codigo) = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Producto> buscar(String texto) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM productos "
                + "WHERE UPPER(nombre) LIKE ? OR UPPER(codigo) LIKE ? ORDER BY categoria, codigo";
        List<Producto> productos = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto.trim().toUpperCase() + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
            }
        }
        return productos;
    }

    public List<Producto> listar() throws SQLException {
        return buscar("");
    }

    public List<Producto> listarPorCategoria(Categoria categoria) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM productos WHERE categoria = ? AND activo = 'S' ORDER BY nombre";
        List<Producto> productos = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categoria.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
            }
        }
        return productos;
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setCategoria(Categoria.valueOf(rs.getString("categoria")));
        producto.setPrecio(rs.getDouble("precio"));
        producto.setExistencia(rs.getInt("existencia"));
        producto.setActivo("S".equals(rs.getString("activo")));
        return producto;
    }
}
