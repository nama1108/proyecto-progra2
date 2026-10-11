package com.panpuntos.vista;

import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.model.Cliente;
import com.panpuntos.model.ItemVenta;
import com.panpuntos.model.Menu;
import com.panpuntos.model.MenuCompleto;
import com.panpuntos.model.Pedido;
import com.panpuntos.model.Producto;
import com.panpuntos.model.ProductoIndividual;
import com.panpuntos.servicio.ClienteService;
import com.panpuntos.servicio.PedidoService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PanelPedidos extends javax.swing.JPanel {

    private final PedidoService pedidoService = new PedidoService();
    private final ClienteService clienteService = new ClienteService();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Tipo", "Descripción", "Cantidad", "Precio unit. (Q)", "Subtotal (Q)", "Puntos"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private List<Producto> productos = new ArrayList<>();
    private List<Menu> menus = new ArrayList<>();
    private Pedido pedido = new Pedido();

    public PanelPedidos() {
        initComponents();
        tblDetalle.setModel(modeloTabla);
        tblDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configurarEventos();
    }

    private void configurarEventos() {
        btnBuscarCliente.addActionListener(e -> buscarCliente());
        txtIdentificacion.addActionListener(e -> buscarCliente());
        cmbTipo.addActionListener(e -> cargarItems());
        cmbItem.addActionListener(e -> mostrarInfoItem());
        btnAgregar.addActionListener(e -> agregarItem());
        btnEliminar.addActionListener(e -> eliminarItem());
        btnLimpiar.addActionListener(e -> limpiarDetalle());
        btnConfirmar.addActionListener(e -> confirmarPedido());
    }

    public void cargarCatalogo() {
        try {
            productos = pedidoService.listarProductosDisponibles();
            menus = pedidoService.listarMenusDisponibles();
            cargarItems();
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private boolean esMenu() {
        return cmbTipo.getSelectedIndex() == 1;
    }

    private void cargarItems() {
        DefaultComboBoxModel<Object> modelo = new DefaultComboBoxModel<>();
        if (esMenu()) {
            for (Menu m : menus) {
                modelo.addElement(m);
            }
        } else {
            for (Producto p : productos) {
                modelo.addElement(p);
            }
        }
        cmbItem.setModel(modelo);
        mostrarInfoItem();
    }

    private void mostrarInfoItem() {
        Object seleccionado = cmbItem.getSelectedItem();
        if (seleccionado instanceof Producto p) {
            int puntos = p.esSandwich() ? ProductoIndividual.PUNTOS_POR_SANDWICH : 0;
            lblInfo.setText(String.format("Precio: Q%.2f   |   Categoría: %s   |   Disponible: %d   |   Puntos por unidad: %d",
                    p.getPrecio(), p.getCategoria(), p.getExistencia(), puntos));
        } else if (seleccionado instanceof Menu m) {
            lblInfo.setText(String.format("Precio: Q%.2f   |   Incluye: %s + %s + %s   |   Puntos por unidad: %d",
                    m.getPrecio(), m.getSandwich().getNombre(), m.getBebida().getNombre(),
                    m.getAcompanamiento().getNombre(), MenuCompleto.PUNTOS_POR_MENU));
        } else {
            lblInfo.setText(" ");
        }
    }

    private void buscarCliente() {
        String identificacion = txtIdentificacion.getText().trim();
        if (identificacion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el NIT o DPI del cliente", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Cliente cliente = clienteService.buscarPorIdentificacion(identificacion);
            if (cliente == null) {
                JOptionPane.showMessageDialog(this, "No existe un cliente con la identificación " + identificacion,
                        "Cliente no encontrado", JOptionPane.WARNING_MESSAGE);
                mostrarCliente(null);
            } else if (!cliente.isActivo()) {
                JOptionPane.showMessageDialog(this, "El cliente " + cliente.getNombre() + " está inactivo",
                        "Cliente inactivo", JOptionPane.WARNING_MESSAGE);
                mostrarCliente(null);
            } else {
                mostrarCliente(cliente);
            }
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void mostrarCliente(Cliente cliente) {
        pedido.setCliente(cliente);
        if (cliente == null) {
            lblCliente.setText("Cliente: (ninguno)");
            lblSaldo.setText("Saldo de puntos: -");
        } else {
            lblCliente.setText("Cliente: " + cliente.getNombre());
            lblSaldo.setText("Saldo de puntos: " + cliente.getPuntos());
        }
    }

    private void agregarItem() {
        Object seleccionado = cmbItem.getSelectedItem();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto o menú", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int cantidad = (Integer) spnCantidad.getValue();
        int indiceExistente = buscarIndice(seleccionado);
        if (indiceExistente >= 0) {
            cantidad += pedido.getItems().get(indiceExistente).getCantidad();
        }
        ItemVenta nuevo = crearItem(seleccionado, cantidad);
        Pedido prueba = new Pedido(pedido.getCliente());
        for (int i = 0; i < pedido.getItems().size(); i++) {
            if (i != indiceExistente) {
                prueba.agregarItem(pedido.getItems().get(i));
            }
        }
        prueba.agregarItem(nuevo);
        try {
            pedidoService.verificarExistencias(prueba);
            if (indiceExistente >= 0) {
                pedido.getItems().set(indiceExistente, nuevo);
            } else {
                pedido.agregarItem(nuevo);
            }
            spnCantidad.setValue(1);
            actualizarDetalle();
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se puede agregar", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private int buscarIndice(Object seleccionado) {
        for (int i = 0; i < pedido.getItems().size(); i++) {
            ItemVenta item = pedido.getItems().get(i);
            if (item instanceof ProductoIndividual individual && seleccionado instanceof Producto p
                    && individual.getProducto().getIdProducto() == p.getIdProducto()) {
                return i;
            }
            if (item instanceof MenuCompleto menuCompleto && seleccionado instanceof Menu m
                    && menuCompleto.getMenu().getIdMenu() == m.getIdMenu()) {
                return i;
            }
        }
        return -1;
    }

    private ItemVenta crearItem(Object seleccionado, int cantidad) {
        if (seleccionado instanceof Menu menu) {
            return new MenuCompleto(menu, cantidad);
        }
        return new ProductoIndividual((Producto) seleccionado, cantidad);
    }

    private void eliminarItem() {
        int fila = tblDetalle.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una línea del detalle", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        pedido.eliminarItem(fila);
        actualizarDetalle();
    }

    private void limpiarDetalle() {
        pedido.getItems().clear();
        actualizarDetalle();
    }

    private void actualizarDetalle() {
        modeloTabla.setRowCount(0);
        for (ItemVenta item : pedido.getItems()) {
            modeloTabla.addRow(new Object[]{
                item instanceof MenuCompleto ? "Menú" : "Producto", item.getNombre(), item.getCantidad(),
                String.format("%.2f", item.getPrecioUnitario()), String.format("%.2f", item.getSubtotal()),
                item.calcularPuntos()
            });
        }
        lblTotal.setText(String.format("Total: Q%.2f", pedido.getTotal()));
        lblPuntos.setText("Puntos que generará: " + pedido.getPuntos());
    }

    private void confirmarPedido() {
        if (pedido.getCliente() != null && !pedido.estaVacio()) {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "Cliente: " + pedido.getCliente().getNombre()
                    + String.format("%nTotal: Q%.2f", pedido.getTotal())
                    + "\nPuntos que generará al pagarse: " + pedido.getPuntos()
                    + "\n\n¿Confirmar la creación del pedido?",
                    "Confirmar pedido", JOptionPane.YES_NO_OPTION);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {
            pedidoService.crear(pedido);
            JOptionPane.showMessageDialog(this, "Pedido #" + pedido.getIdPedido() + " registrado como PENDIENTE");
            nuevoPedido();
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se puede confirmar", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void nuevoPedido() {
        pedido = new Pedido();
        txtIdentificacion.setText("");
        mostrarCliente(null);
        actualizarDetalle();
        cargarCatalogo();
    }

    private void mostrarError(SQLException e) {
        JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSuperior = new javax.swing.JPanel();
        pnlCliente = new javax.swing.JPanel();
        lblIdentificacion = new javax.swing.JLabel();
        txtIdentificacion = new javax.swing.JTextField();
        btnBuscarCliente = new javax.swing.JButton();
        lblCliente = new javax.swing.JLabel();
        lblSaldo = new javax.swing.JLabel();
        pnlCatalogo = new javax.swing.JPanel();
        pnlSeleccion = new javax.swing.JPanel();
        lblTipo = new javax.swing.JLabel();
        cmbTipo = new javax.swing.JComboBox<>();
        lblItem = new javax.swing.JLabel();
        cmbItem = new javax.swing.JComboBox<>();
        lblCantidad = new javax.swing.JLabel();
        spnCantidad = new javax.swing.JSpinner();
        btnAgregar = new javax.swing.JButton();
        lblInfo = new javax.swing.JLabel();
        pnlDetalle = new javax.swing.JPanel();
        scrDetalle = new javax.swing.JScrollPane();
        tblDetalle = new javax.swing.JTable();
        pnlAccionesDetalle = new javax.swing.JPanel();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        pnlTotales = new javax.swing.JPanel();
        pnlResumen = new javax.swing.JPanel();
        lblTotal = new javax.swing.JLabel();
        lblPuntos = new javax.swing.JLabel();
        pnlConfirmar = new javax.swing.JPanel();
        btnConfirmar = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new java.awt.BorderLayout());

        pnlSuperior.setLayout(new java.awt.BorderLayout());

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

        lblSaldo.setText("Saldo de puntos: -");
        pnlCliente.add(lblSaldo);

        pnlSuperior.add(pnlCliente, java.awt.BorderLayout.NORTH);

        pnlCatalogo.setBorder(javax.swing.BorderFactory.createTitledBorder("Catálogo de productos y menús"));
        pnlCatalogo.setLayout(new java.awt.BorderLayout());

        pnlSeleccion.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblTipo.setText("Tipo:");
        pnlSeleccion.add(lblTipo);

        cmbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Producto individual", "Menú completo" }));
        pnlSeleccion.add(cmbTipo);

        lblItem.setText("Ítem:");
        pnlSeleccion.add(lblItem);
        pnlSeleccion.add(cmbItem);

        lblCantidad.setText("Cantidad:");
        pnlSeleccion.add(lblCantidad);

        spnCantidad.setModel(new javax.swing.SpinnerNumberModel(1, 1, 100, 1));
        pnlSeleccion.add(spnCantidad);

        btnAgregar.setText("Agregar al pedido");
        pnlSeleccion.add(btnAgregar);

        pnlCatalogo.add(pnlSeleccion, java.awt.BorderLayout.CENTER);

        lblInfo.setText(" ");
        lblInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 8, 6, 8));
        pnlCatalogo.add(lblInfo, java.awt.BorderLayout.SOUTH);

        pnlSuperior.add(pnlCatalogo, java.awt.BorderLayout.SOUTH);

        add(pnlSuperior, java.awt.BorderLayout.NORTH);

        pnlDetalle.setBorder(javax.swing.BorderFactory.createTitledBorder("Detalle del pedido"));
        pnlDetalle.setLayout(new java.awt.BorderLayout());

        scrDetalle.setViewportView(tblDetalle);

        pnlDetalle.add(scrDetalle, java.awt.BorderLayout.CENTER);

        pnlAccionesDetalle.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnEliminar.setText("Eliminar seleccionado");
        pnlAccionesDetalle.add(btnEliminar);

        btnLimpiar.setText("Limpiar pedido");
        pnlAccionesDetalle.add(btnLimpiar);

        pnlDetalle.add(pnlAccionesDetalle, java.awt.BorderLayout.SOUTH);

        add(pnlDetalle, java.awt.BorderLayout.CENTER);

        pnlTotales.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 0, 0));
        pnlTotales.setLayout(new java.awt.BorderLayout());

        pnlResumen.setLayout(new java.awt.GridLayout(0, 1, 0, 4));

        lblTotal.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        lblTotal.setText("Total: Q0.00");
        pnlResumen.add(lblTotal);

        lblPuntos.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        lblPuntos.setText("Puntos que generará: 0");
        pnlResumen.add(lblPuntos);

        pnlTotales.add(pnlResumen, java.awt.BorderLayout.WEST);

        pnlConfirmar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnConfirmar.setText("Confirmar pedido");
        pnlConfirmar.add(btnConfirmar);

        pnlTotales.add(pnlConfirmar, java.awt.BorderLayout.EAST);

        add(pnlTotales, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnBuscarCliente;
    private javax.swing.JButton btnConfirmar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JComboBox<Object> cmbItem;
    private javax.swing.JComboBox<String> cmbTipo;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblIdentificacion;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblItem;
    private javax.swing.JLabel lblPuntos;
    private javax.swing.JLabel lblSaldo;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel pnlAccionesDetalle;
    private javax.swing.JPanel pnlCatalogo;
    private javax.swing.JPanel pnlCliente;
    private javax.swing.JPanel pnlConfirmar;
    private javax.swing.JPanel pnlDetalle;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlSeleccion;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JPanel pnlTotales;
    private javax.swing.JScrollPane scrDetalle;
    private javax.swing.JSpinner spnCantidad;
    private javax.swing.JTable tblDetalle;
    private javax.swing.JTextField txtIdentificacion;
    // End of variables declaration//GEN-END:variables
}
