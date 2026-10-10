package com.panpuntos.servicio;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.dao.MovimientoInventarioDAO;
import com.panpuntos.dao.ProductoDAO;
import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.model.MovimientoInventario;
import com.panpuntos.model.Producto;
import com.panpuntos.model.TipoMovimientoInventario;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class InventarioService {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoInventarioDAO movimientoDAO = new MovimientoInventarioDAO();

    public MovimientoInventario abastecer(Producto producto, int cantidad) throws ValidacionException, SQLException {
        if (producto == null) {
            throw new ValidacionException("Seleccione un producto");
        }
        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad a abastecer debe ser mayor a cero");
        }
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                MovimientoInventario movimiento = registrarMovimiento(con, producto.getIdProducto(), cantidad,
                        TipoMovimientoInventario.ABASTECIMIENTO, "ABASTECIMIENTO");
                con.commit();
                return movimiento;
            } catch (SQLException | ValidacionException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public MovimientoInventario registrarMovimiento(Connection con, int idProducto, int cantidad,
            TipoMovimientoInventario tipo, String referencia) throws ValidacionException, SQLException {
        Producto actual = productoDAO.buscarParaActualizar(con, idProducto);
        if (actual == null) {
            throw new ValidacionException("El producto ya no existe");
        }
        int anterior = actual.getExistencia();
        int nueva = tipo == TipoMovimientoInventario.ABASTECIMIENTO ? anterior + cantidad : anterior - cantidad;
        if (nueva < 0) {
            throw new ValidacionException("Existencia insuficiente de " + actual.getNombre()
                    + ". Disponible: " + anterior + ", solicitado: " + cantidad);
        }
        productoDAO.actualizarExistencia(con, idProducto, nueva);
        MovimientoInventario movimiento = new MovimientoInventario(actual, tipo, cantidad, anterior, nueva, referencia);
        movimientoDAO.insertar(con, movimiento);
        actual.setExistencia(nueva);
        return movimiento;
    }

    public List<Producto> listarProductosActivos() throws SQLException {
        return productoDAO.listarActivos();
    }

    public List<MovimientoInventario> listarMovimientos(Producto producto) throws SQLException {
        return movimientoDAO.listar(producto == null ? null : producto.getIdProducto());
    }
}
