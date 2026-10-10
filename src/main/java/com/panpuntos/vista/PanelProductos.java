package com.panpuntos.vista;

import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.model.Categoria;
import com.panpuntos.model.Producto;
import com.panpuntos.servicio.ProductoService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PanelProductos extends javax.swing.JPanel {

    private final ProductoService productoService = new ProductoService();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Código", "Nombre", "Categoría", "Precio (Q)", "Existencia", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private List<Producto> productos = new ArrayList<>();
    private Producto productoSeleccionado;

    public PanelProductos() {
        initComponents();
        tblProductos.setModel(modeloTabla);
        tblProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configurarEventos();
        limpiar();
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnCambiarEstado.addActionListener(e -> cambiarEstado());
        btnBuscar.addActionListener(e -> cargarProductos());
        txtBuscar.addActionListener(e -> cargarProductos());
        tblProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });
    }

    public void cargarProductos() {
        try {
            productos = productoService.buscar(txtBuscar.getText());
            modeloTabla.setRowCount(0);
            for (Producto p : productos) {
                modeloTabla.addRow(new Object[]{
                    p.getCodigo(), p.getNombre(), p.getCategoria(), String.format("%.2f", p.getPrecio()),
                    p.getExistencia(), p.isActivo() ? "Activo" : "Inactivo"
                });
            }
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void seleccionar() {
        int fila = tblProductos.getSelectedRow();
        if (fila < 0) {
            return;
        }
        productoSeleccionado = productos.get(fila);
        txtCodigo.setText(productoSeleccionado.getCodigo());
        txtNombre.setText(productoSeleccionado.getNombre());
        cmbCategoria.setSelectedItem(productoSeleccionado.getCategoria().name());
        txtPrecio.setText(String.format("%.2f", productoSeleccionado.getPrecio()));
        txtExistencia.setText(String.valueOf(productoSeleccionado.getExistencia()));
        txtExistencia.setEditable(false);
        lblExistencia.setText("Existencia (ver Inventario):");
        btnCambiarEstado.setEnabled(true);
        btnCambiarEstado.setText(productoSeleccionado.isActivo() ? "Desactivar" : "Activar");
    }

    private void limpiar() {
        productoSeleccionado = null;
        txtCodigo.setText("");
        txtNombre.setText("");
        cmbCategoria.setSelectedIndex(0);
        txtPrecio.setText("");
        txtExistencia.setText("0");
        txtExistencia.setEditable(true);
        lblExistencia.setText("Existencia inicial:");
        btnCambiarEstado.setEnabled(false);
        btnCambiarEstado.setText("Desactivar");
        tblProductos.clearSelection();
        cargarProductos();
        txtCodigo.requestFocus();
    }

    private void guardar() {
        try {
            Producto producto = new Producto();
            if (productoSeleccionado != null) {
                producto.setIdProducto(productoSeleccionado.getIdProducto());
            }
            producto.setCodigo(txtCodigo.getText().trim().toUpperCase());
            producto.setNombre(txtNombre.getText().trim());
            producto.setCategoria(Categoria.valueOf((String) cmbCategoria.getSelectedItem()));
            producto.setPrecio(leerPrecio());
            if (productoSeleccionado == null) {
                producto.setExistencia(leerExistencia());
                productoService.registrar(producto);
                JOptionPane.showMessageDialog(this, "Producto registrado correctamente");
            } else {
                productoService.actualizar(producto);
                JOptionPane.showMessageDialog(this, "Producto actualizado correctamente");
            }
            limpiar();
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private double leerPrecio() throws ValidacionException {
        try {
            return Double.parseDouble(txtPrecio.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValidacionException("El precio debe ser un número (ej. 25.00)");
        }
    }

    private int leerExistencia() throws ValidacionException {
        try {
            return Integer.parseInt(txtExistencia.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidacionException("La existencia debe ser un número entero");
        }
    }

    private void cambiarEstado() {
        if (productoSeleccionado == null) {
            return;
        }
        boolean activar = !productoSeleccionado.isActivo();
        String accion = activar ? "activar" : "desactivar";
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Desea " + accion + " el producto " + productoSeleccionado.getNombre() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            productoService.cambiarActivo(productoSeleccionado, activar);
            limpiar();
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
        lblCodigo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblCategoria = new javax.swing.JLabel();
        cmbCategoria = new javax.swing.JComboBox<>();
        lblPrecio = new javax.swing.JLabel();
        txtPrecio = new javax.swing.JTextField();
        lblExistencia = new javax.swing.JLabel();
        txtExistencia = new javax.swing.JTextField();
        pnlBotones = new javax.swing.JPanel();
        btnNuevo = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnCambiarEstado = new javax.swing.JButton();
        pnlLista = new javax.swing.JPanel();
        pnlBuscar = new javax.swing.JPanel();
        lblBuscar = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        scrProductos = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new java.awt.BorderLayout());

        pnlDatos.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del producto"));
        pnlDatos.setLayout(new java.awt.BorderLayout());

        pnlCampos.setLayout(new java.awt.GridLayout(0, 4, 10, 6));

        lblCodigo.setText("Código:");
        pnlCampos.add(lblCodigo);
        pnlCampos.add(txtCodigo);

        lblNombre.setText("Nombre:");
        pnlCampos.add(lblNombre);
        pnlCampos.add(txtNombre);

        lblCategoria.setText("Categoría:");
        pnlCampos.add(lblCategoria);

        cmbCategoria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SANDWICH", "BEBIDA", "ACOMPANAMIENTO" }));
        pnlCampos.add(cmbCategoria);

        lblPrecio.setText("Precio (Q):");
        pnlCampos.add(lblPrecio);
        pnlCampos.add(txtPrecio);

        lblExistencia.setText("Existencia inicial:");
        pnlCampos.add(lblExistencia);
        pnlCampos.add(txtExistencia);

        pnlDatos.add(pnlCampos, java.awt.BorderLayout.CENTER);

        pnlBotones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnNuevo.setText("Nuevo");
        pnlBotones.add(btnNuevo);

        btnGuardar.setText("Guardar");
        pnlBotones.add(btnGuardar);

        btnCambiarEstado.setText("Desactivar");
        pnlBotones.add(btnCambiarEstado);

        pnlDatos.add(pnlBotones, java.awt.BorderLayout.SOUTH);

        add(pnlDatos, java.awt.BorderLayout.NORTH);

        pnlLista.setBorder(javax.swing.BorderFactory.createTitledBorder("Productos"));
        pnlLista.setLayout(new java.awt.BorderLayout());

        pnlBuscar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblBuscar.setText("Buscar (código o nombre):");
        pnlBuscar.add(lblBuscar);

        txtBuscar.setColumns(25);
        pnlBuscar.add(txtBuscar);

        btnBuscar.setText("Buscar");
        pnlBuscar.add(btnBuscar);

        pnlLista.add(pnlBuscar, java.awt.BorderLayout.NORTH);

        scrProductos.setViewportView(tblProductos);

        pnlLista.add(scrProductos, java.awt.BorderLayout.CENTER);

        add(pnlLista, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCambiarEstado;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JComboBox<String> cmbCategoria;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblExistencia;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblPrecio;
    private javax.swing.JPanel pnlBotones;
    private javax.swing.JPanel pnlBuscar;
    private javax.swing.JPanel pnlCampos;
    private javax.swing.JPanel pnlDatos;
    private javax.swing.JPanel pnlLista;
    private javax.swing.JScrollPane scrProductos;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtExistencia;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPrecio;
    // End of variables declaration//GEN-END:variables
}
