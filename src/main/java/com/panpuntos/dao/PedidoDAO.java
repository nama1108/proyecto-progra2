package com.panpuntos.dao;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.model.Cliente;
import com.panpuntos.model.EstadoPedido;
import com.panpuntos.model.ItemVenta;
import com.panpuntos.model.MenuCompleto;
import com.panpuntos.model.Pedido;
import com.panpuntos.model.ProductoIndividual;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    private static final String CONSULTA = "SELECT p.id_pedido, p.fecha, p.estado, "
            + "c.id_cliente, c.tipo_identificacion, c.identificacion, c.nombre, c.puntos, c.activo "
            + "FROM pedidos p JOIN clientes c ON c.id_cliente = p.id_cliente ";

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MenuDAO menuDAO = new MenuDAO();

    public void insertar(Connection con, Pedido pedido) throws SQLException {
        String sql = "INSERT INTO pedidos (id_cliente, estado, total, puntos) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"id_pedido"})) {
            ps.setInt(1, pedido.getCliente().getIdCliente());
            ps.setString(2, pedido.getEstado().name());
            ps.setDouble(3, pedido.getTotal());
            ps.setInt(4, pedido.getPuntos());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pedido.setIdPedido(rs.getInt(1));
                }
            }
        }
        for (ItemVenta item : pedido.getItems()) {
            insertarDetalle(con, pedido.getIdPedido(), item);
        }
    }

    private void insertarDetalle(Connection con, int idPedido, ItemVenta item) throws SQLException {
        String sql = "INSERT INTO detalle_pedido (id_pedido, tipo_item, id_producto, id_menu, cantidad, precio_unitario, subtotal, puntos) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"id_detalle"})) {
            ps.setInt(1, idPedido);
            ps.setString(2, item.getTipo());
            if (item instanceof ProductoIndividual individual) {
                ps.setInt(3, individual.getProducto().getIdProducto());
                ps.setNull(4, Types.INTEGER);
            } else if (item instanceof MenuCompleto menu) {
                ps.setNull(3, Types.INTEGER);
                ps.setInt(4, menu.getMenu().getIdMenu());
            }
            ps.setInt(5, item.getCantidad());
            ps.setDouble(6, item.getPrecioUnitario());
            ps.setDouble(7, item.getSubtotal());
            ps.setInt(8, item.calcularPuntos());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setIdDetalle(rs.getInt(1));
                }
            }
        }
    }

    public void cambiarEstado(Connection con, int idPedido, EstadoPedido estado) throws SQLException {
        String sql = "UPDATE pedidos SET estado = ? WHERE id_pedido = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        }
    }

    public Pedido buscarPorId(Connection con, int idPedido, boolean bloquear) throws SQLException {
        String sql = CONSULTA + "WHERE p.id_pedido = ?" + (bloquear ? " FOR UPDATE OF p.estado" : "");
        Pedido pedido = null;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pedido = mapear(rs);
                }
            }
        }
        if (pedido != null) {
            cargarItems(con, pedido);
        }
        return pedido;
    }

    public Pedido buscarPorId(int idPedido) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            return buscarPorId(con, idPedido, false);
        }
    }

    public List<Pedido> listarPorEstado(EstadoPedido estado) throws SQLException {
        return listar("WHERE p.estado = ? ORDER BY p.fecha DESC, p.id_pedido DESC", estado.name());
    }

    public List<Pedido> listarPorCliente(int idCliente) throws SQLException {
        return listar("WHERE p.id_cliente = ? ORDER BY p.fecha DESC, p.id_pedido DESC", idCliente);
    }

    private List<Pedido> listar(String condicion, Object parametro) throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion()) {
            try (PreparedStatement ps = con.prepareStatement(CONSULTA + condicion)) {
                ps.setObject(1, parametro);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        pedidos.add(mapear(rs));
                    }
                }
            }
            for (Pedido pedido : pedidos) {
                cargarItems(con, pedido);
            }
        }
        return pedidos;
    }

    private void cargarItems(Connection con, Pedido pedido) throws SQLException {
        String sql = "SELECT id_detalle, tipo_item, id_producto, id_menu, cantidad, precio_unitario "
                + "FROM detalle_pedido WHERE id_pedido = ? ORDER BY id_detalle";
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pedido.getIdPedido());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{rs.getInt("id_detalle"), rs.getString("tipo_item"), rs.getInt("id_producto"),
                        rs.getInt("id_menu"), rs.getInt("cantidad"), rs.getDouble("precio_unitario")});
                }
            }
        }
        for (Object[] fila : filas) {
            ItemVenta item;
            if ("MENU".equals(fila[1])) {
                item = new MenuCompleto(menuDAO.buscarPorId(con, (Integer) fila[3]), (Integer) fila[4]);
            } else {
                item = new ProductoIndividual(productoDAO.buscarPorId(con, (Integer) fila[2]), (Integer) fila[4]);
            }
            item.setIdDetalle((Integer) fila[0]);
            item.setPrecioUnitario((Double) fila[5]);
            pedido.agregarItem(item);
        }
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setTipoIdentificacion(rs.getString("tipo_identificacion"));
        cliente.setIdentificacion(rs.getString("identificacion"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setPuntos(rs.getInt("puntos"));
        cliente.setActivo("S".equals(rs.getString("activo")));
        Pedido pedido = new Pedido(cliente);
        pedido.setIdPedido(rs.getInt("id_pedido"));
        pedido.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        pedido.setEstado(EstadoPedido.valueOf(rs.getString("estado")));
        return pedido;
    }
}
