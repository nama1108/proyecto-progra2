package com.panpuntos.vista;

import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.model.Cliente;
import com.panpuntos.model.MovimientoPuntos;
import com.panpuntos.model.Pedido;
import com.panpuntos.servicio.ClienteService;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PanelClientes extends javax.swing.JPanel {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ClienteService clienteService = new ClienteService();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Tipo", "Identificación", "Nombre", "Teléfono", "Correo", "Puntos", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private List<Cliente> clientes = new ArrayList<>();
    private Cliente clienteSeleccionado;

    public PanelClientes() {
        initComponents();
        tblClientes.setModel(modeloTabla);
        tblClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configurarEventos();
        limpiar();
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnCambiarEstado.addActionListener(e -> cambiarEstado());
        btnVerPedidos.addActionListener(e -> verPedidos());
        btnHistorial.addActionListener(e -> verHistorial());
        btnBuscar.addActionListener(e -> cargarClientes());
        txtBuscar.addActionListener(e -> cargarClientes());
        tblClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });
    }

    public void cargarClientes() {
        try {
            clientes = clienteService.buscar(txtBuscar.getText());
            modeloTabla.setRowCount(0);
            for (Cliente c : clientes) {
                modeloTabla.addRow(new Object[]{
                    c.getTipoIdentificacion(), c.getIdentificacion(), c.getNombre(), c.getTelefono(),
                    c.getCorreo(), c.getPuntos(), c.isActivo() ? "Activo" : "Inactivo"
                });
            }
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void seleccionar() {
        int fila = tblClientes.getSelectedRow();
        if (fila < 0) {
            return;
        }
        clienteSeleccionado = clientes.get(fila);
        cmbTipo.setSelectedItem(clienteSeleccionado.getTipoIdentificacion());
        txtIdentificacion.setText(clienteSeleccionado.getIdentificacion());
        txtNombre.setText(clienteSeleccionado.getNombre());
        txtTelefono.setText(clienteSeleccionado.getTelefono());
        txtCorreo.setText(clienteSeleccionado.getCorreo());
        txtPuntos.setText(String.valueOf(clienteSeleccionado.getPuntos()));
        btnCambiarEstado.setEnabled(true);
        btnVerPedidos.setEnabled(true);
        btnHistorial.setEnabled(true);
        btnCambiarEstado.setText(clienteSeleccionado.isActivo() ? "Desactivar" : "Activar");
    }

    private void limpiar() {
        clienteSeleccionado = null;
        cmbTipo.setSelectedIndex(0);
        txtIdentificacion.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtPuntos.setText("0");
        btnCambiarEstado.setEnabled(false);
        btnVerPedidos.setEnabled(false);
        btnHistorial.setEnabled(false);
        btnCambiarEstado.setText("Desactivar");
        tblClientes.clearSelection();
        cargarClientes();
        txtIdentificacion.requestFocus();
    }

    private void guardar() {
        Cliente cliente = new Cliente();
        if (clienteSeleccionado != null) {
            cliente.setIdCliente(clienteSeleccionado.getIdCliente());
        }
        cliente.setTipoIdentificacion((String) cmbTipo.getSelectedItem());
        cliente.setIdentificacion(txtIdentificacion.getText().trim());
        cliente.setNombre(txtNombre.getText().trim());
        cliente.setTelefono(txtTelefono.getText().trim());
        cliente.setCorreo(txtCorreo.getText().trim());
        try {
            if (clienteSeleccionado == null) {
                clienteService.registrar(cliente);
                JOptionPane.showMessageDialog(this, "Cliente registrado correctamente");
            } else {
                clienteService.actualizar(cliente);
                JOptionPane.showMessageDialog(this, "Cliente actualizado correctamente");
            }
            limpiar();
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void cambiarEstado() {
        if (clienteSeleccionado == null) {
            return;
        }
        boolean activar = !clienteSeleccionado.isActivo();
        String accion = activar ? "activar" : "desactivar";
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Desea " + accion + " al cliente " + clienteSeleccionado.getNombre() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            clienteService.cambiarActivo(clienteSeleccionado, activar);
            limpiar();
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void verPedidos() {
        if (clienteSeleccionado == null) {
            return;
        }
        try {
            List<Object[]> filas = new ArrayList<>();
            for (Pedido p : clienteService.listarPedidos(clienteSeleccionado)) {
                filas.add(new Object[]{"#" + p.getIdPedido(), p.getFecha().format(FORMATO_FECHA), p.getEstado(),
                    String.format("%.2f", p.getTotal()), p.getPuntos()});
            }
            Dialogos.mostrarTabla(this, "Pedidos de " + clienteSeleccionado.getNombre(),
                    new Object[]{"Pedido", "Fecha", "Estado", "Total (Q)", "Puntos"}, filas);
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void verHistorial() {
        if (clienteSeleccionado == null) {
            return;
        }
        try {
            List<Object[]> filas = new ArrayList<>();
            for (MovimientoPuntos m : clienteService.listarHistorialPuntos(clienteSeleccionado)) {
                filas.add(new Object[]{m.getFecha().format(FORMATO_FECHA), m.getTipo(), m.getReferencia(),
                    (m.getPuntos() > 0 ? "+" : "") + m.getPuntos()});
            }
            Dialogos.mostrarTabla(this, "Historial de puntos de " + clienteSeleccionado.getNombre()
                    + " (saldo: " + clienteSeleccionado.getPuntos() + ")",
                    new Object[]{"Fecha", "Tipo", "Referencia", "Puntos"}, filas);
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

        pnlDatos = new javax.swing.JPanel();
        pnlCampos = new javax.swing.JPanel();
        lblTipo = new javax.swing.JLabel();
        cmbTipo = new javax.swing.JComboBox<>();
        lblIdentificacion = new javax.swing.JLabel();
        txtIdentificacion = new javax.swing.JTextField();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblTelefono = new javax.swing.JLabel();
        txtTelefono = new javax.swing.JTextField();
        lblCorreo = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        lblPuntos = new javax.swing.JLabel();
        txtPuntos = new javax.swing.JTextField();
        pnlBotones = new javax.swing.JPanel();
        btnNuevo = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnCambiarEstado = new javax.swing.JButton();
        btnVerPedidos = new javax.swing.JButton();
        btnHistorial = new javax.swing.JButton();
        pnlLista = new javax.swing.JPanel();
        pnlBuscar = new javax.swing.JPanel();
        lblBuscar = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        scrClientes = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new java.awt.BorderLayout());

        pnlDatos.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del cliente"));
        pnlDatos.setLayout(new java.awt.BorderLayout());

        pnlCampos.setLayout(new java.awt.GridLayout(0, 4, 10, 6));

        lblTipo.setText("Tipo de identificación:");
        pnlCampos.add(lblTipo);

        cmbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NIT", "DPI" }));
        pnlCampos.add(cmbTipo);

        lblIdentificacion.setText("Identificación:");
        pnlCampos.add(lblIdentificacion);
        pnlCampos.add(txtIdentificacion);

        lblNombre.setText("Nombre:");
        pnlCampos.add(lblNombre);
        pnlCampos.add(txtNombre);

        lblTelefono.setText("Teléfono:");
        pnlCampos.add(lblTelefono);
        pnlCampos.add(txtTelefono);

        lblCorreo.setText("Correo:");
        pnlCampos.add(lblCorreo);
        pnlCampos.add(txtCorreo);

        lblPuntos.setText("Puntos acumulados:");
        pnlCampos.add(lblPuntos);

        txtPuntos.setEditable(false);
        txtPuntos.setText("0");
        pnlCampos.add(txtPuntos);

        pnlDatos.add(pnlCampos, java.awt.BorderLayout.CENTER);

        pnlBotones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnNuevo.setText("Nuevo");
        pnlBotones.add(btnNuevo);

        btnGuardar.setText("Guardar");
        pnlBotones.add(btnGuardar);

        btnCambiarEstado.setText("Desactivar");
        pnlBotones.add(btnCambiarEstado);

        btnVerPedidos.setText("Ver pedidos");
        pnlBotones.add(btnVerPedidos);

        btnHistorial.setText("Historial de puntos");
        pnlBotones.add(btnHistorial);

        pnlDatos.add(pnlBotones, java.awt.BorderLayout.SOUTH);

        add(pnlDatos, java.awt.BorderLayout.NORTH);

        pnlLista.setBorder(javax.swing.BorderFactory.createTitledBorder("Clientes"));
        pnlLista.setLayout(new java.awt.BorderLayout());

        pnlBuscar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblBuscar.setText("Buscar (nombre o identificación):");
        pnlBuscar.add(lblBuscar);

        txtBuscar.setColumns(25);
        pnlBuscar.add(txtBuscar);

        btnBuscar.setText("Buscar");
        pnlBuscar.add(btnBuscar);

        pnlLista.add(pnlBuscar, java.awt.BorderLayout.NORTH);

        scrClientes.setViewportView(tblClientes);

        pnlLista.add(scrClientes, java.awt.BorderLayout.CENTER);

        add(pnlLista, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCambiarEstado;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnHistorial;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnVerPedidos;
    private javax.swing.JComboBox<String> cmbTipo;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblIdentificacion;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblPuntos;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JPanel pnlBotones;
    private javax.swing.JPanel pnlBuscar;
    private javax.swing.JPanel pnlCampos;
    private javax.swing.JPanel pnlDatos;
    private javax.swing.JPanel pnlLista;
    private javax.swing.JScrollPane scrClientes;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtIdentificacion;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPuntos;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
