package com.panpuntos.vista;

import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.model.Canje;
import com.panpuntos.model.Cliente;
import com.panpuntos.model.MovimientoPuntos;
import com.panpuntos.model.Producto;
import com.panpuntos.model.Recompensa;
import com.panpuntos.servicio.ClienteService;
import com.panpuntos.servicio.FidelizacionService;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PanelFidelizacion extends javax.swing.JPanel {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final FidelizacionService fidelizacionService = new FidelizacionService();
    private final ClienteService clienteService = new ClienteService();
    private final DefaultTableModel modeloRecompensas = crearModelo("Recompensa", "Puntos necesarios", "Incluye", "Disponible");
    private final DefaultTableModel modeloHistorial = crearModelo("Fecha", "Tipo", "Referencia", "Puntos");
    private List<Recompensa> recompensas = new ArrayList<>();
    private Cliente cliente;

    public PanelFidelizacion() {
        initComponents();
        tblRecompensas.setModel(modeloRecompensas);
        tblRecompensas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHistorial.setModel(modeloHistorial);
        configurarEventos();
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
        btnBuscarCliente.addActionListener(e -> buscarCliente());
        txtIdentificacion.addActionListener(e -> buscarCliente());
        btnCanjear.addActionListener(e -> canjear());
    }

    public void cargarDatos() {
        try {
            recompensas = fidelizacionService.listarRecompensas();
            modeloRecompensas.setRowCount(0);
            for (Recompensa r : recompensas) {
                List<String> nombres = new ArrayList<>();
                for (Producto p : r.getProductosInventario()) {
                    nombres.add(p.getNombre());
                }
                modeloRecompensas.addRow(new Object[]{
                    r.getNombre(), r.getPuntosNecesarios(), String.join(" + ", nombres), r.estaDisponible() ? "Sí" : "No"
                });
            }
            if (cliente != null) {
                cliente = clienteService.buscarPorIdentificacion(cliente.getIdentificacion());
                mostrarCliente();
            }
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void buscarCliente() {
        String identificacion = txtIdentificacion.getText().trim();
        if (identificacion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el NIT o DPI del cliente", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            cliente = clienteService.buscarPorIdentificacion(identificacion);
            if (cliente == null) {
                JOptionPane.showMessageDialog(this, "No existe un cliente con la identificación " + identificacion,
                        "Cliente no encontrado", JOptionPane.WARNING_MESSAGE);
            }
            mostrarCliente();
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void mostrarCliente() throws SQLException {
        modeloHistorial.setRowCount(0);
        if (cliente == null) {
            lblCliente.setText("Cliente: (ninguno)");
            lblSaldo.setText("Saldo de puntos: -");
            return;
        }
        lblCliente.setText("Cliente: " + cliente.getNombre() + (cliente.isActivo() ? "" : " (INACTIVO)"));
        lblSaldo.setText("Saldo de puntos: " + cliente.getPuntos());
        for (MovimientoPuntos m : fidelizacionService.listarHistorial(cliente)) {
            modeloHistorial.addRow(new Object[]{
                m.getFecha().format(FORMATO_FECHA), m.getTipo(), m.getReferencia(),
                (m.getPuntos() > 0 ? "+" : "") + m.getPuntos()
            });
        }
    }

    private void canjear() {
        int fila = tblRecompensas.getSelectedRow();
        if (cliente == null) {
            JOptionPane.showMessageDialog(this, "Busque primero al cliente", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una recompensa del catálogo", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Recompensa recompensa = recompensas.get(fila);
        int respuesta = JOptionPane.showConfirmDialog(this,
                "Cliente: " + cliente.getNombre()
                + "\nRecompensa: " + recompensa.getNombre()
                + "\nPuntos a descontar: " + recompensa.getPuntosNecesarios()
                + "\nSaldo actual: " + cliente.getPuntos()
                + "\n\n¿Confirmar el canje?",
                "Confirmar canje", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Canje canje = fidelizacionService.canjear(cliente.getIdCliente(), recompensa.getIdRecompensa());
            Dialogos.mostrarTexto(this, "Comprobante de canje", fidelizacionService.generarComprobanteCanje(canje));
            cargarDatos();
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Canje rechazado", JOptionPane.WARNING_MESSAGE);
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

        pnlCliente = new javax.swing.JPanel();
        lblIdentificacion = new javax.swing.JLabel();
        txtIdentificacion = new javax.swing.JTextField();
        btnBuscarCliente = new javax.swing.JButton();
        lblCliente = new javax.swing.JLabel();
        lblSaldo = new javax.swing.JLabel();
        pnlCentro = new javax.swing.JPanel();
        pnlRecompensas = new javax.swing.JPanel();
        scrRecompensas = new javax.swing.JScrollPane();
        tblRecompensas = new javax.swing.JTable();
        pnlCanje = new javax.swing.JPanel();
        btnCanjear = new javax.swing.JButton();
        pnlHistorial = new javax.swing.JPanel();
        scrHistorial = new javax.swing.JScrollPane();
        tblHistorial = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new java.awt.BorderLayout());

        pnlCliente.setBorder(javax.swing.BorderFactory.createTitledBorder("Cliente"));
        pnlCliente.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblIdentificacion.setText("NIT / DPI:");
        pnlCliente.add(lblIdentificacion);

        txtIdentificacion.setColumns(15);
        pnlCliente.add(txtIdentificacion);

        btnBuscarCliente.setText("Buscar cliente");
        pnlCliente.add(btnBuscarCliente);

        lblCliente.setText("Cliente: (ninguno)");
        pnlCliente.add(lblCliente);

        lblSaldo.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        lblSaldo.setText("Saldo de puntos: -");
        pnlCliente.add(lblSaldo);

        add(pnlCliente, java.awt.BorderLayout.NORTH);

        pnlCentro.setLayout(new java.awt.GridLayout(1, 2, 10, 0));

        pnlRecompensas.setBorder(javax.swing.BorderFactory.createTitledBorder("Catálogo de recompensas"));
        pnlRecompensas.setLayout(new java.awt.BorderLayout());

        scrRecompensas.setViewportView(tblRecompensas);

        pnlRecompensas.add(scrRecompensas, java.awt.BorderLayout.CENTER);

        pnlCanje.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnCanjear.setText("Canjear recompensa seleccionada");
        pnlCanje.add(btnCanjear);

        pnlRecompensas.add(pnlCanje, java.awt.BorderLayout.SOUTH);

        pnlCentro.add(pnlRecompensas);

        pnlHistorial.setBorder(javax.swing.BorderFactory.createTitledBorder("Historial de puntos"));
        pnlHistorial.setLayout(new java.awt.BorderLayout());

        scrHistorial.setViewportView(tblHistorial);

        pnlHistorial.add(scrHistorial, java.awt.BorderLayout.CENTER);

        pnlCentro.add(pnlHistorial);

        add(pnlCentro, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarCliente;
    private javax.swing.JButton btnCanjear;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblIdentificacion;
    private javax.swing.JLabel lblSaldo;
    private javax.swing.JPanel pnlCanje;
    private javax.swing.JPanel pnlCentro;
    private javax.swing.JPanel pnlCliente;
    private javax.swing.JPanel pnlHistorial;
    private javax.swing.JPanel pnlRecompensas;
    private javax.swing.JScrollPane scrHistorial;
    private javax.swing.JScrollPane scrRecompensas;
    private javax.swing.JTable tblHistorial;
    private javax.swing.JTable tblRecompensas;
    private javax.swing.JTextField txtIdentificacion;
    // End of variables declaration//GEN-END:variables
}
