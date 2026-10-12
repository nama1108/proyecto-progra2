package com.panpuntos.vista;

import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.model.Pago;
import com.panpuntos.model.PagoEfectivo;
import com.panpuntos.model.PagoTarjeta;
import com.panpuntos.servicio.CobroService;
import java.awt.Component;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public final class AsistenteCobro {

    private static final CobroService COBRO_SERVICE = new CobroService();

    private AsistenteCobro() {
    }

    public static boolean cobrar(Component padre, int idPedido, double total, String metodo) {
        try {
            Pago pago = "TARJETA".equals(metodo)
                    ? cobrarConTarjeta(padre, idPedido, total)
                    : cobrarEnEfectivo(padre, idPedido, total);
            if (pago == null || !pago.esAprobado()) {
                return false;
            }
            if (pago instanceof PagoEfectivo efectivo) {
                JOptionPane.showMessageDialog(padre, String.format("Pago aprobado.%nCambio a entregar: Q%.2f", efectivo.getCambio()));
            }
            mostrarComprobante(padre, idPedido);
            return true;
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(padre, e.getMessage(), "No se puede cobrar", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(padre, "Error de base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private static Pago cobrarEnEfectivo(Component padre, int idPedido, double total) throws ValidacionException, SQLException {
        while (true) {
            String texto = JOptionPane.showInputDialog(padre,
                    String.format("Pedido #%d%nTotal a pagar: Q%.2f%n%nIngrese el dinero recibido:", idPedido, total),
                    "Pago en efectivo", JOptionPane.QUESTION_MESSAGE);
            if (texto == null) {
                return null;
            }
            double recibido;
            try {
                recibido = Double.parseDouble(texto.trim().replace(",", "."));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(padre, "Ingrese un monto válido (ej. 150.00)", "Validación", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            Pago pago = COBRO_SERVICE.cobrar(idPedido, new PagoEfectivo(idPedido, total, recibido));
            if (pago.esAprobado()) {
                return pago;
            }
            JOptionPane.showMessageDialog(padre,
                    String.format("Pago rechazado: dinero insuficiente.%nFaltan Q%.2f. Ingrese un nuevo monto.", total - recibido),
                    "Pago rechazado", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static Pago cobrarConTarjeta(Component padre, int idPedido, double total) throws ValidacionException, SQLException {
        String referencia = JOptionPane.showInputDialog(padre,
                String.format("Pedido #%d%nMonto a cobrar: Q%.2f%n%nNúmero de referencia de la transacción:", idPedido, total),
                "TRX-" + System.currentTimeMillis() % 1000000);
        if (referencia == null) {
            return null;
        }
        if (referencia.isBlank()) {
            throw new ValidacionException("La referencia de la transacción es obligatoria");
        }
        int respuesta = JOptionPane.showConfirmDialog(padre,
                "Simulación de pago con tarjeta.\n¿La operación fue APROBADA por el banco?",
                "Pago con tarjeta", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.CLOSED_OPTION) {
            return null;
        }
        Pago pago = COBRO_SERVICE.cobrar(idPedido,
                new PagoTarjeta(idPedido, total, referencia.trim(), respuesta == JOptionPane.YES_OPTION));
        if (!pago.esAprobado()) {
            JOptionPane.showMessageDialog(padre,
                    "Pago con tarjeta RECHAZADO.\nEl pedido sigue PENDIENTE; no se descontó inventario ni se acreditaron puntos.",
                    "Pago rechazado", JOptionPane.WARNING_MESSAGE);
        }
        return pago;
    }

    public static void mostrarComprobante(Component padre, int idPedido) {
        try {
            Dialogos.mostrarTexto(padre, "Comprobante de venta", COBRO_SERVICE.generarComprobante(idPedido));
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(padre, e.getMessage(), "Comprobante", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(padre, "Error de base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
