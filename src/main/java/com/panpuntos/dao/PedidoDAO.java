package com.panpuntos.dao;

import com.panpuntos.model.ItemVenta;
import com.panpuntos.model.MenuCompleto;
import com.panpuntos.model.Pedido;
import com.panpuntos.model.ProductoIndividual;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class PedidoDAO {

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
}
