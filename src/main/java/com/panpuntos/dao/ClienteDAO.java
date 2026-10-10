package com.panpuntos.dao;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private static final String COLUMNAS = "id_cliente, tipo_identificacion, identificacion, nombre, telefono, correo, puntos, activo, fecha_registro";

    public void insertar(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO clientes (tipo_identificacion, identificacion, nombre, telefono, correo) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"id_cliente"})) {
            ps.setString(1, cliente.getTipoIdentificacion());
            ps.setString(2, cliente.getIdentificacion());
            ps.setString(3, cliente.getNombre());
            ps.setString(4, cliente.getTelefono());
            ps.setString(5, cliente.getCorreo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setIdCliente(rs.getInt(1));
                }
            }
        }
    }

    public void actualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE clientes SET tipo_identificacion = ?, identificacion = ?, nombre = ?, telefono = ?, correo = ? WHERE id_cliente = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getTipoIdentificacion());
            ps.setString(2, cliente.getIdentificacion());
            ps.setString(3, cliente.getNombre());
            ps.setString(4, cliente.getTelefono());
            ps.setString(5, cliente.getCorreo());
            ps.setInt(6, cliente.getIdCliente());
            ps.executeUpdate();
        }
    }

    public void cambiarActivo(int idCliente, boolean activo) throws SQLException {
        String sql = "UPDATE clientes SET activo = ? WHERE id_cliente = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, activo ? "S" : "N");
            ps.setInt(2, idCliente);
            ps.executeUpdate();
        }
    }

    public void sumarPuntos(Connection con, int idCliente, int puntos) throws SQLException {
        String sql = "UPDATE clientes SET puntos = puntos + ? WHERE id_cliente = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, puntos);
            ps.setInt(2, idCliente);
            ps.executeUpdate();
        }
    }

    public Cliente buscarPorId(int idCliente) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM clientes WHERE id_cliente = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Cliente buscarPorIdentificacion(String identificacion) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM clientes WHERE identificacion = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, identificacion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Cliente> buscar(String texto) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM clientes "
                + "WHERE UPPER(nombre) LIKE ? OR identificacion LIKE ? ORDER BY nombre";
        List<Cliente> clientes = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto.trim().toUpperCase() + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapear(rs));
                }
            }
        }
        return clientes;
    }

    public List<Cliente> listar() throws SQLException {
        return buscar("");
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setTipoIdentificacion(rs.getString("tipo_identificacion"));
        cliente.setIdentificacion(rs.getString("identificacion"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setCorreo(rs.getString("correo"));
        cliente.setPuntos(rs.getInt("puntos"));
        cliente.setActivo("S".equals(rs.getString("activo")));
        cliente.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        return cliente;
    }
}
