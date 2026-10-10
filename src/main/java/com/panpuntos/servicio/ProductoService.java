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

public class ProductoService {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoInventarioDAO movimientoDAO = new MovimientoInventarioDAO();

    public void registrar(Producto producto) throws ValidacionException, SQLException {
        validar(producto);
        if (producto.getExistencia() < 0) {
            throw new ValidacionException("La existencia no puede ser negativa");
        }
        if (productoDAO.buscarPorCodigo(producto.getCodigo()) != null) {
            throw new ValidacionException("Ya existe un producto con el código " + producto.getCodigo());
        }
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                productoDAO.insertar(con, producto);
                if (producto.getExistencia() > 0) {
                    movimientoDAO.insertar(con, new MovimientoInventario(producto, TipoMovimientoInventario.ABASTECIMIENTO,
                            producto.getExistencia(), 0, producto.getExistencia(), "INVENTARIO INICIAL"));
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public void actualizar(Producto producto) throws ValidacionException, SQLException {
        validar(producto);
        Producto existente = productoDAO.buscarPorCodigo(producto.getCodigo());
        if (existente != null && existente.getIdProducto() != producto.getIdProducto()) {
            throw new ValidacionException("Ya existe otro producto con el código " + producto.getCodigo());
        }
        productoDAO.actualizar(producto);
    }

    public void cambiarActivo(Producto producto, boolean activo) throws SQLException {
        productoDAO.cambiarActivo(producto.getIdProducto(), activo);
        producto.setActivo(activo);
    }

    public List<Producto> buscar(String texto) throws SQLException {
        return productoDAO.buscar(texto);
    }

    private void validar(Producto producto) throws ValidacionException {
        if (producto.getCodigo() == null || producto.getCodigo().isBlank()) {
            throw new ValidacionException("El código es obligatorio");
        }
        if (producto.getCodigo().length() > 10) {
            throw new ValidacionException("El código no puede tener más de 10 caracteres");
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new ValidacionException("El nombre es obligatorio");
        }
        if (producto.getCategoria() == null) {
            throw new ValidacionException("Seleccione una categoría");
        }
        if (producto.getPrecio() < 0) {
            throw new ValidacionException("El precio no puede ser negativo");
        }
    }
}
