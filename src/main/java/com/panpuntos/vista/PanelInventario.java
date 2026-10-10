package com.panpuntos.vista;

import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.model.MovimientoInventario;
import com.panpuntos.model.Producto;
import com.panpuntos.servicio.InventarioService;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class PanelInventario extends javax.swing.JPanel {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final InventarioService inventarioService = new InventarioService();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Fecha", "Código", "Producto", "Tipo", "Cantidad", "Anterior", "Nueva", "Referencia"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public PanelInventario() {
        initComponents();
        tblMovimientos.setModel(modeloTabla);
        configurarEventos();
    }

    private void configurarEventos() {
        cmbProducto.addActionListener(e -> {
            mostrarExistencia();
            if (chkSoloProducto.isSelected()) {
                cargarMovimientos();
            }
        });
        chkSoloProducto.addActionListener(e -> cargarMovimientos());
        btnAbastecer.addActionListener(e -> abastecer());
        txtCantidad.addActionListener(e -> abastecer());
    }

    public void cargarDatos() {
        try {
            Producto seleccionado = getProductoSeleccionado();
            DefaultComboBoxModel<Producto> modelo = new DefaultComboBoxModel<>();
            for (Producto p : inventarioService.listarProductosActivos()) {
                modelo.addElement(p);
                if (seleccionado != null && p.getIdProducto() == seleccionado.getIdProducto()) {
                    modelo.setSelectedItem(p);
                }
            }
            cmbProducto.setModel(modelo);
            mostrarExistencia();
            cargarMovimientos();
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private Producto getProductoSeleccionado() {
        return (Producto) cmbProducto.getSelectedItem();
    }

    private void mostrarExistencia() {
        Producto producto = getProductoSeleccionado();
        txtExistencia.setText(producto == null ? "" : String.valueOf(producto.getExistencia()));
    }

    private void cargarMovimientos() {
        try {
            Producto filtro = chkSoloProducto.isSelected() ? getProductoSeleccionado() : null;
            modeloTabla.setRowCount(0);
            for (MovimientoInventario m : inventarioService.listarMovimientos(filtro)) {
                modeloTabla.addRow(new Object[]{
                    m.getFecha().format(FORMATO_FECHA), m.getProducto().getCodigo(), m.getProducto().getNombre(),
                    m.getTipo(), m.getCantidad(), m.getExistenciaAnterior(), m.getExistenciaNueva(), m.getReferencia()
                });
            }
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void abastecer() {
        Producto producto = getProductoSeleccionado();
        try {
            int cantidad = leerCantidad();
            if (producto == null) {
                throw new ValidacionException("Seleccione un producto");
            }
            if (cantidad <= 0) {
                throw new ValidacionException("La cantidad a abastecer debe ser mayor a cero");
            }
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "Producto: " + producto.getNombre()
                    + "\nExistencia actual: " + producto.getExistencia()
                    + "\nIngreso: +" + cantidad
                    + "\nExistencia final: " + (producto.getExistencia() + cantidad)
                    + "\n\n¿Confirmar abastecimiento?",
                    "Confirmar abastecimiento", JOptionPane.YES_NO_OPTION);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
            MovimientoInventario movimiento = inventarioService.abastecer(producto, cantidad);
            JOptionPane.showMessageDialog(this, "Abastecimiento registrado. Existencia de "
                    + producto.getNombre() + ": " + movimiento.getExistenciaNueva());
            txtCantidad.setText("");
            cargarDatos();
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private int leerCantidad() throws ValidacionException {
        try {
            return Integer.parseInt(txtCantidad.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidacionException("La cantidad debe ser un número entero");
        }
    }

    private void mostrarError(SQLException e) {
        JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlAbastecer = new javax.swing.JPanel();
        pnlCampos = new javax.swing.JPanel();
        lblProducto = new javax.swing.JLabel();
        cmbProducto = new javax.swing.JComboBox<>();
        lblExistencia = new javax.swing.JLabel();
        txtExistencia = new javax.swing.JTextField();
        lblCantidad = new javax.swing.JLabel();
        txtCantidad = new javax.swing.JTextField();
        pnlBotones = new javax.swing.JPanel();
        btnAbastecer = new javax.swing.JButton();
        pnlHistorial = new javax.swing.JPanel();
        pnlFiltro = new javax.swing.JPanel();
        chkSoloProducto = new javax.swing.JCheckBox();
        scrMovimientos = new javax.swing.JScrollPane();
        tblMovimientos = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new java.awt.BorderLayout());

        pnlAbastecer.setBorder(javax.swing.BorderFactory.createTitledBorder("Abastecer producto"));
        pnlAbastecer.setLayout(new java.awt.BorderLayout());

        pnlCampos.setLayout(new java.awt.GridLayout(0, 4, 10, 6));

        lblProducto.setText("Producto:");
        pnlCampos.add(lblProducto);
        pnlCampos.add(cmbProducto);

        lblExistencia.setText("Existencia actual:");
        pnlCampos.add(lblExistencia);

        txtExistencia.setEditable(false);
        pnlCampos.add(txtExistencia);

        lblCantidad.setText("Cantidad a ingresar:");
        pnlCampos.add(lblCantidad);
        pnlCampos.add(txtCantidad);

        pnlAbastecer.add(pnlCampos, java.awt.BorderLayout.CENTER);

        pnlBotones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnAbastecer.setText("Abastecer");
        pnlBotones.add(btnAbastecer);

        pnlAbastecer.add(pnlBotones, java.awt.BorderLayout.SOUTH);

        add(pnlAbastecer, java.awt.BorderLayout.NORTH);

        pnlHistorial.setBorder(javax.swing.BorderFactory.createTitledBorder("Historial de movimientos"));
        pnlHistorial.setLayout(new java.awt.BorderLayout());

        pnlFiltro.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        chkSoloProducto.setText("Mostrar solo el producto seleccionado");
        pnlFiltro.add(chkSoloProducto);

        pnlHistorial.add(pnlFiltro, java.awt.BorderLayout.NORTH);

        scrMovimientos.setViewportView(tblMovimientos);

        pnlHistorial.add(scrMovimientos, java.awt.BorderLayout.CENTER);

        add(pnlHistorial, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbastecer;
    private javax.swing.JCheckBox chkSoloProducto;
    private javax.swing.JComboBox<com.panpuntos.model.Producto> cmbProducto;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblExistencia;
    private javax.swing.JLabel lblProducto;
    private javax.swing.JPanel pnlAbastecer;
    private javax.swing.JPanel pnlBotones;
    private javax.swing.JPanel pnlCampos;
    private javax.swing.JPanel pnlFiltro;
    private javax.swing.JPanel pnlHistorial;
    private javax.swing.JScrollPane scrMovimientos;
    private javax.swing.JTable tblMovimientos;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtExistencia;
    // End of variables declaration//GEN-END:variables
}
