package com.panpuntos.servicio;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.dao.MenuDAO;
import com.panpuntos.dao.PedidoDAO;
import com.panpuntos.dao.ProductoDAO;
import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.model.EstadoPedido;
import com.panpuntos.model.ItemVenta;
import com.panpuntos.model.Menu;
import com.panpuntos.model.MenuCompleto;
import com.panpuntos.model.Pedido;
import com.panpuntos.model.Producto;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PedidoService {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MenuDAO menuDAO = new MenuDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    public List<Producto> listarProductosDisponibles() throws SQLException {
        return productoDAO.listarActivos();
    }

    public List<Menu> listarMenusDisponibles() throws SQLException {
        return menuDAO.listarActivos();
    }

    public void verificarExistencias(Pedido pedido) throws ValidacionException, SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            verificarExistencias(con, pedido);
        }
    }

    public void verificarExistencias(Connection con, Pedido pedido) throws ValidacionException, SQLException {
        Map<Integer, Integer> requeridos = new LinkedHashMap<>();
        for (ItemVenta item : pedido.getItems()) {
            if (item instanceof MenuCompleto menuCompleto) {
                Menu actual = menuDAO.buscarPorId(con, menuCompleto.getMenu().getIdMenu());
                if (actual == null || !actual.isActivo()) {
                    throw new ValidacionException("El menú " + menuCompleto.getNombre() + " no está disponible");
                }
            }
            for (Producto producto : item.getProductosInventario()) {
                requeridos.merge(producto.getIdProducto(), item.getCantidad(), Integer::sum);
            }
        }
        for (Map.Entry<Integer, Integer> requerido : requeridos.entrySet()) {
            Producto actual = productoDAO.buscarPorId(con, requerido.getKey());
            if (actual == null || !actual.isActivo()) {
                throw new ValidacionException("Un producto del pedido ya no está disponible");
            }
            if (actual.getExistencia() < requerido.getValue()) {
                throw new ValidacionException("Existencias insuficientes de " + actual.getNombre()
                        + ".\nDisponible: " + actual.getExistencia() + ", requerido en el pedido: " + requerido.getValue());
            }
        }
    }

    public void crear(Pedido pedido) throws ValidacionException, SQLException {
        if (pedido.getCliente() == null) {
            throw new ValidacionException("Busque y seleccione un cliente");
        }
        if (!pedido.getCliente().isActivo()) {
            throw new ValidacionException("El cliente está inactivo");
        }
        if (pedido.estaVacio()) {
            throw new ValidacionException("Agregue al menos un producto o menú al pedido");
        }
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                verificarExistencias(con, pedido);
                pedido.setEstado(EstadoPedido.PENDIENTE);
                pedido.setFecha(LocalDateTime.now());
                pedidoDAO.insertar(con, pedido);
                con.commit();
            } catch (SQLException | ValidacionException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
