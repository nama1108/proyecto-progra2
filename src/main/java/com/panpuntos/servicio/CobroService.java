package com.panpuntos.servicio;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.dao.ClienteDAO;
import com.panpuntos.dao.MovimientoPuntosDAO;
import com.panpuntos.dao.PagoDAO;
import com.panpuntos.dao.PedidoDAO;
import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.model.EstadoPedido;
import com.panpuntos.model.ItemVenta;
import com.panpuntos.model.MovimientoPuntos;
import com.panpuntos.model.Pago;
import com.panpuntos.model.PagoEfectivo;
import com.panpuntos.model.PagoTarjeta;
import com.panpuntos.model.Pedido;
import com.panpuntos.model.Producto;
import com.panpuntos.model.TipoMovimientoInventario;
import com.panpuntos.model.TipoMovimientoPuntos;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CobroService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final MovimientoPuntosDAO movimientoPuntosDAO = new MovimientoPuntosDAO();
    private final PedidoService pedidoService = new PedidoService();
    private final InventarioService inventarioService = new InventarioService();

    public List<Pedido> listarPorEstado(EstadoPedido estado) throws SQLException {
        return pedidoDAO.listarPorEstado(estado);
    }

    public Pago cobrar(int idPedido, Pago pago) throws ValidacionException, SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                Pedido pedido = pedidoDAO.buscarPorId(con, idPedido, true);
                if (pedido == null) {
                    throw new ValidacionException("El pedido #" + idPedido + " no existe");
                }
                if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
                    throw new ValidacionException("Solo se pueden cobrar pedidos PENDIENTES. El pedido #"
                            + idPedido + " está " + pedido.getEstado());
                }
                pedidoService.verificarExistencias(con, pedido);
                pago.setIdPedido(idPedido);
                pago.setMonto(pedido.getTotal());
                pago.setFecha(LocalDateTime.now());
                pago.procesar();
                pagoDAO.insertar(con, pago);
                if (pago.esAprobado()) {
                    registrarVenta(con, pedido);
                }
                con.commit();
                return pago;
            } catch (SQLException | ValidacionException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private void registrarVenta(Connection con, Pedido pedido) throws ValidacionException, SQLException {
        String referencia = "PEDIDO #" + pedido.getIdPedido();
        for (ItemVenta item : pedido.getItems()) {
            for (Producto producto : item.getProductosInventario()) {
                inventarioService.registrarMovimiento(con, producto.getIdProducto(), item.getCantidad(),
                        TipoMovimientoInventario.VENTA, referencia);
            }
        }
        int puntos = pedido.getPuntos();
        if (puntos > 0) {
            clienteDAO.sumarPuntos(con, pedido.getCliente().getIdCliente(), puntos);
            MovimientoPuntos movimiento = new MovimientoPuntos();
            movimiento.setIdCliente(pedido.getCliente().getIdCliente());
            movimiento.setTipo(TipoMovimientoPuntos.ACUMULACION);
            movimiento.setPuntos(puntos);
            movimiento.setIdPedido(pedido.getIdPedido());
            movimientoPuntosDAO.insertar(con, movimiento);
        }
        pedido.cambiarEstado(EstadoPedido.PAGADO);
        pedidoDAO.cambiarEstado(con, pedido.getIdPedido(), EstadoPedido.PAGADO);
    }

    public void anular(int idPedido) throws ValidacionException, SQLException {
        cambiarEstado(idPedido, EstadoPedido.ANULADO);
    }

    public void entregar(int idPedido) throws ValidacionException, SQLException {
        cambiarEstado(idPedido, EstadoPedido.ENTREGADO);
    }

    private void cambiarEstado(int idPedido, EstadoPedido nuevoEstado) throws ValidacionException, SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                Pedido pedido = pedidoDAO.buscarPorId(con, idPedido, true);
                if (pedido == null) {
                    throw new ValidacionException("El pedido #" + idPedido + " no existe");
                }
                pedido.cambiarEstado(nuevoEstado);
                pedidoDAO.cambiarEstado(con, idPedido, nuevoEstado);
                con.commit();
            } catch (SQLException | ValidacionException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public String generarComprobante(int idPedido) throws ValidacionException, SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            Pedido pedido = pedidoDAO.buscarPorId(con, idPedido, false);
            Pago pago = pagoDAO.buscarAprobado(con, idPedido);
            if (pedido == null || pago == null) {
                throw new ValidacionException("El pedido #" + idPedido + " no tiene un pago aprobado");
            }
            String linea = "-".repeat(46) + "\n";
            StringBuilder sb = new StringBuilder();
            sb.append("            PAN, PUNTOS Y PREMIOS\n");
            sb.append("             Comprobante de venta\n");
            sb.append(linea);
            sb.append(String.format("Pedido #%d%n", pedido.getIdPedido()));
            sb.append(String.format("Fecha: %s%n", pago.getFecha().format(FORMATO_FECHA)));
            sb.append(String.format("Cliente: %s%n", pedido.getCliente().getNombre()));
            sb.append(String.format("%s: %s%n", pedido.getCliente().getTipoIdentificacion(), pedido.getCliente().getIdentificacion()));
            sb.append(linea);
            sb.append(String.format("%-4s %-28s %12s%n", "Cant", "Descripción", "Subtotal"));
            for (ItemVenta item : pedido.getItems()) {
                sb.append(String.format("%4d %-28.28s %12.2f%n", item.getCantidad(), item.getNombre(), item.getSubtotal()));
            }
            sb.append(linea);
            sb.append(String.format("%-33s %12s%n", "TOTAL", String.format("Q%.2f", pedido.getTotal())));
            sb.append(String.format("Método de pago: %s%n", pago.getMetodo()));
            if (pago instanceof PagoEfectivo efectivo) {
                sb.append(String.format("Recibido: Q%.2f    Cambio: Q%.2f%n", efectivo.getMontoRecibido(), efectivo.getCambio()));
            } else if (pago instanceof PagoTarjeta tarjeta) {
                sb.append(String.format("Referencia: %s%n", tarjeta.getReferencia()));
            }
            sb.append(linea);
            sb.append(String.format("Puntos ganados en esta compra: %d%n", pedido.getPuntos()));
            sb.append(String.format("Saldo actual de puntos: %d%n", pedido.getCliente().getPuntos()));
            sb.append(linea);
            sb.append("           ¡Gracias por su compra!\n");
            return sb.toString();
        }
    }
}
