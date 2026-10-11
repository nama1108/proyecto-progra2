package com.panpuntos.vista;

import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.model.EstadoPedido;
import com.panpuntos.model.ItemVenta;
import com.panpuntos.model.Pedido;
import com.panpuntos.servicio.CobroService;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PanelCobro extends javax.swing.JPanel {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CobroService cobroService = new CobroService();
    private final DefaultTableModel modeloPedidos = crearModelo("Pedido", "Fecha", "Cliente", "Total (Q)", "Puntos", "Estado");
    private final DefaultTableModel modeloDetalle = crearModelo("Descripción", "Cantidad", "Subtotal (Q)", "Puntos");
    private List<Pedido> pedidos = new ArrayList<>();

    public PanelCobro() {
        initComponents();
        tblPedidos.setModel(modeloPedidos);
        tblPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDetalle.setModel(modeloDetalle);
        configurarEventos();
        actualizarBotones();
    }

    private static DefaultTableModel crearModelo(Object... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private void configurarEventos() {
        cmbEstado.addActionListener(e -> cargarPedidos());
        btnActualizar.addActionListener(e -> cargarPedidos());
        tblPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });
        btnCobrar.addActionListener(e -> cobrar());
        btnComprobante.addActionListener(e -> verComprobante());
        btnEntregar.addActionListener(e -> entregar());
        btnAnular.addActionListener(e -> anular());
    }

    public void cargarPedidos() {
        try {
            EstadoPedido estado = EstadoPedido.valueOf((String) cmbEstado.getSelectedItem());
            pedidos = cobroService.listarPorEstado(estado);
            modeloPedidos.setRowCount(0);
            for (Pedido p : pedidos) {
                modeloPedidos.addRow(new Object[]{
                    "#" + p.getIdPedido(), p.getFecha().format(FORMATO_FECHA), p.getCliente().getNombre(),
                    String.format("%.2f", p.getTotal()), p.getPuntos(), p.getEstado()
                });
            }
            mostrarDetalle();
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private Pedido getPedidoSeleccionado() {
        int fila = tblPedidos.getSelectedRow();
        return fila < 0 ? null : pedidos.get(fila);
    }

    private void mostrarDetalle() {
        Pedido pedido = getPedidoSeleccionado();
        modeloDetalle.setRowCount(0);
        if (pedido == null) {
            lblTotal.setText("Total: Q0.00");
        } else {
            for (ItemVenta item : pedido.getItems()) {
                modeloDetalle.addRow(new Object[]{
                    item.getNombre(), item.getCantidad(), String.format("%.2f", item.getSubtotal()), item.calcularPuntos()
                });
            }
            lblTotal.setText(String.format("Total: Q%.2f   |   Puntos: %d   |   Cliente: %s",
                    pedido.getTotal(), pedido.getPuntos(), pedido.getCliente().getNombre()));
        }
        actualizarBotones();
    }

    private void actualizarBotones() {
        Pedido pedido = getPedidoSeleccionado();
        EstadoPedido estado = pedido == null ? null : pedido.getEstado();
        btnCobrar.setEnabled(estado == EstadoPedido.PENDIENTE);
        btnAnular.setEnabled(estado == EstadoPedido.PENDIENTE);
        btnEntregar.setEnabled(estado == EstadoPedido.PAGADO);
        btnComprobante.setEnabled(estado == EstadoPedido.PAGADO || estado == EstadoPedido.ENTREGADO);
        cmbMetodo.setEnabled(estado == EstadoPedido.PENDIENTE);
    }

    private void cobrar() {
        Pedido pedido = getPedidoSeleccionado();
        if (pedido != null && AsistenteCobro.cobrar(this, pedido.getIdPedido(), pedido.getTotal(), (String) cmbMetodo.getSelectedItem())) {
            cargarPedidos();
        }
    }

    private void verComprobante() {
        Pedido pedido = getPedidoSeleccionado();
        if (pedido != null) {
            AsistenteCobro.mostrarComprobante(this, pedido.getIdPedido());
        }
    }

    private void entregar() {
        Pedido pedido = getPedidoSeleccionado();
        if (pedido == null) {
            return;
        }
        try {
            cobroService.entregar(pedido.getIdPedido());
            JOptionPane.showMessageDialog(this, "Pedido #" + pedido.getIdPedido() + " marcado como ENTREGADO");
            cargarPedidos();
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void anular() {
        Pedido pedido = getPedidoSeleccionado();
        if (pedido == null) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Anular el pedido #" + pedido.getIdPedido() + " de " + pedido.getCliente().getNombre() + "?"
                + "\nNo se modificará el inventario ni los puntos.",
                "Anular pedido", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            cobroService.anular(pedido.getIdPedido());
            cargarPedidos();
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void mostrarError(SQLException e) {
        JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFiltro = new javax.swing.JPanel();
        lblEstado = new javax.swing.JLabel();
        cmbEstado = new javax.swing.JComboBox<>();
        btnActualizar = new javax.swing.JButton();
        pnlCentro = new javax.swing.JPanel();
        pnlPedidos = new javax.swing.JPanel();
        scrPedidos = new javax.swing.JScrollPane();
        tblPedidos = new javax.swing.JTable();
        pnlDetalle = new javax.swing.JPanel();
        scrDetalle = new javax.swing.JScrollPane();
        tblDetalle = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        pnlAcciones = new javax.swing.JPanel();
        lblMetodo = new javax.swing.JLabel();
        cmbMetodo = new javax.swing.JComboBox<>();
        btnCobrar = new javax.swing.JButton();
        btnComprobante = new javax.swing.JButton();
        btnEntregar = new javax.swing.JButton();
        btnAnular = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new java.awt.BorderLayout());

        pnlFiltro.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblEstado.setText("Pedidos en estado:");
        pnlFiltro.add(lblEstado);

        cmbEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PENDIENTE", "PAGADO", "ENTREGADO", "ANULADO" }));
        pnlFiltro.add(cmbEstado);

        btnActualizar.setText("Actualizar");
        pnlFiltro.add(btnActualizar);

        add(pnlFiltro, java.awt.BorderLayout.NORTH);

        pnlCentro.setLayout(new java.awt.GridLayout(1, 2, 10, 0));

        pnlPedidos.setBorder(javax.swing.BorderFactory.createTitledBorder("Pedidos"));
        pnlPedidos.setLayout(new java.awt.BorderLayout());

        scrPedidos.setViewportView(tblPedidos);

        pnlPedidos.add(scrPedidos, java.awt.BorderLayout.CENTER);

        pnlCentro.add(pnlPedidos);

        pnlDetalle.setBorder(javax.swing.BorderFactory.createTitledBorder("Detalle del pedido seleccionado"));
        pnlDetalle.setLayout(new java.awt.BorderLayout());

        scrDetalle.setViewportView(tblDetalle);

        pnlDetalle.add(scrDetalle, java.awt.BorderLayout.CENTER);

        lblTotal.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        lblTotal.setText("Total: Q0.00");
        pnlDetalle.add(lblTotal, java.awt.BorderLayout.SOUTH);

        pnlCentro.add(pnlDetalle);

        add(pnlCentro, java.awt.BorderLayout.CENTER);

        pnlAcciones.setBorder(javax.swing.BorderFactory.createTitledBorder("Acciones"));
        pnlAcciones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblMetodo.setText("Método de pago:");
        pnlAcciones.add(lblMetodo);

        cmbMetodo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "EFECTIVO", "TARJETA" }));
        pnlAcciones.add(cmbMetodo);

        btnCobrar.setText("Cobrar");
        pnlAcciones.add(btnCobrar);

        btnComprobante.setText("Ver comprobante");
        pnlAcciones.add(btnComprobante);

        btnEntregar.setText("Marcar entregado");
        pnlAcciones.add(btnEntregar);

        btnAnular.setText("Anular pedido");
        pnlAcciones.add(btnAnular);

        add(pnlAcciones, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnAnular;
    private javax.swing.JButton btnCobrar;
    private javax.swing.JButton btnComprobante;
    private javax.swing.JButton btnEntregar;
    private javax.swing.JComboBox<String> cmbEstado;
    private javax.swing.JComboBox<String> cmbMetodo;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblMetodo;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel pnlAcciones;
    private javax.swing.JPanel pnlCentro;
    private javax.swing.JPanel pnlDetalle;
    private javax.swing.JPanel pnlFiltro;
    private javax.swing.JPanel pnlPedidos;
    private javax.swing.JScrollPane scrDetalle;
    private javax.swing.JScrollPane scrPedidos;
    private javax.swing.JTable tblDetalle;
    private javax.swing.JTable tblPedidos;
    // End of variables declaration//GEN-END:variables
}
