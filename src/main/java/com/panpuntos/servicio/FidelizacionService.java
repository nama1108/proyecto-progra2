package com.panpuntos.servicio;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.dao.CanjeDAO;
import com.panpuntos.dao.ClienteDAO;
import com.panpuntos.dao.MovimientoPuntosDAO;
import com.panpuntos.dao.RecompensaDAO;
import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.model.Canje;
import com.panpuntos.model.Cliente;
import com.panpuntos.model.MovimientoPuntos;
import com.panpuntos.model.Producto;
import com.panpuntos.model.Recompensa;
import com.panpuntos.model.TipoMovimientoInventario;
import com.panpuntos.model.TipoMovimientoPuntos;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FidelizacionService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final RecompensaDAO recompensaDAO = new RecompensaDAO();
    private final CanjeDAO canjeDAO = new CanjeDAO();
    private final MovimientoPuntosDAO movimientoPuntosDAO = new MovimientoPuntosDAO();
    private final InventarioService inventarioService = new InventarioService();

    public List<Recompensa> listarRecompensas() throws SQLException {
        return recompensaDAO.listarActivas();
    }

    public List<MovimientoPuntos> listarHistorial(Cliente cliente) throws SQLException {
        return movimientoPuntosDAO.listarPorCliente(cliente.getIdCliente());
    }

    public Canje canjear(int idCliente, int idRecompensa) throws ValidacionException, SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                Cliente cliente = clienteDAO.buscarParaActualizar(con, idCliente);
                if (cliente == null) {
                    throw new ValidacionException("El cliente no existe");
                }
                if (!cliente.isActivo()) {
                    throw new ValidacionException("El cliente " + cliente.getNombre() + " está inactivo");
                }
                Recompensa recompensa = recompensaDAO.buscarPorId(con, idRecompensa);
                if (recompensa == null || !recompensa.isActivo()) {
                    throw new ValidacionException("La recompensa no está disponible");
                }
                if (!cliente.tienePuntosSuficientes(recompensa.getPuntosNecesarios())) {
                    throw new ValidacionException("Puntos insuficientes.\nSaldo: " + cliente.getPuntos()
                            + ", necesarios: " + recompensa.getPuntosNecesarios());
                }
                Canje canje = new Canje(cliente, recompensa);
                canjeDAO.insertar(con, canje);
                for (Producto producto : recompensa.getProductosInventario()) {
                    inventarioService.registrarMovimiento(con, producto.getIdProducto(), 1,
                            TipoMovimientoInventario.CANJE, "CANJE #" + canje.getIdCanje());
                }
                clienteDAO.sumarPuntos(con, idCliente, -canje.getPuntosUsados());
                MovimientoPuntos movimiento = new MovimientoPuntos();
                movimiento.setIdCliente(idCliente);
                movimiento.setTipo(TipoMovimientoPuntos.CANJE);
                movimiento.setPuntos(-canje.getPuntosUsados());
                movimiento.setIdCanje(canje.getIdCanje());
                movimientoPuntosDAO.insertar(con, movimiento);
                con.commit();
                canje.setFecha(LocalDateTime.now());
                cliente.setPuntos(cliente.getPuntos() - canje.getPuntosUsados());
                return canje;
            } catch (SQLException | ValidacionException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public String generarComprobanteCanje(Canje canje) {
        String linea = "-".repeat(46) + "\n";
        StringBuilder sb = new StringBuilder();
        sb.append("            PAN, PUNTOS Y PREMIOS\n");
        sb.append("             Comprobante de canje\n");
        sb.append(linea);
        sb.append(String.format("Canje #%d%n", canje.getIdCanje()));
        sb.append(String.format("Fecha: %s%n", canje.getFecha().format(FORMATO_FECHA)));
        sb.append(String.format("Cliente: %s%n", canje.getCliente().getNombre()));
        sb.append(String.format("%s: %s%n", canje.getCliente().getTipoIdentificacion(), canje.getCliente().getIdentificacion()));
        sb.append(linea);
        sb.append(String.format("Recompensa: %s%n", canje.getRecompensa().getNombre()));
        for (Producto producto : canje.getRecompensa().getProductosInventario()) {
            sb.append(String.format("   1 x %s%n", producto.getNombre()));
        }
        sb.append(linea);
        sb.append(String.format("Puntos utilizados: %d%n", canje.getPuntosUsados()));
        sb.append(String.format("Saldo restante: %d%n", canje.getCliente().getPuntos()));
        sb.append(linea);
        sb.append("        ¡Gracias por su preferencia!\n");
        return sb.toString();
    }
}
