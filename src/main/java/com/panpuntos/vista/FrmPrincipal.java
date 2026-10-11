package com.panpuntos.vista;

import java.awt.CardLayout;

public class FrmPrincipal extends javax.swing.JFrame {

    private final PanelClientes panelClientes = new PanelClientes();
    private final PanelProductos panelProductos = new PanelProductos();
    private final PanelInventario panelInventario = new PanelInventario();
    private final PanelPedidos panelPedidos = new PanelPedidos();

    public FrmPrincipal() {
        initComponents();
        pnlContenido.add(panelClientes, "clientes");
        pnlContenido.add(panelProductos, "productos");
        pnlContenido.add(panelInventario, "inventario");
        pnlContenido.add(panelPedidos, "pedidos");
        configurarEventos();
    }

    private void configurarEventos() {
        btnClientes.addActionListener(e -> {
            panelClientes.cargarClientes();
            mostrarPanel("clientes");
        });
        btnProductos.addActionListener(e -> {
            panelProductos.cargarProductos();
            mostrarPanel("productos");
        });
        btnInventario.addActionListener(e -> {
            panelInventario.cargarDatos();
            mostrarPanel("inventario");
        });
        btnPedidos.addActionListener(e -> {
            panelPedidos.cargarCatalogo();
            mostrarPanel("pedidos");
        });
        btnSalir.addActionListener(e -> dispose());
    }

    public void mostrarPanel(String nombre) {
        CardLayout layout = (CardLayout) pnlContenido.getLayout();
        layout.show(pnlContenido, nombre);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitulo = new javax.swing.JLabel();
        pnlMenu = new javax.swing.JPanel();
        pnlBotones = new javax.swing.JPanel();
        btnClientes = new javax.swing.JButton();
        btnProductos = new javax.swing.JButton();
        btnInventario = new javax.swing.JButton();
        btnPedidos = new javax.swing.JButton();
        btnCobro = new javax.swing.JButton();
        btnFidelizacion = new javax.swing.JButton();
        btnReportes = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        pnlContenido = new javax.swing.JPanel();
        lblBienvenida = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Pan, Puntos y Premios");

        lblTitulo.setFont(new java.awt.Font("Dialog", 1, 22)); // NOI18N
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("Pan, Puntos y Premios");
        lblTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().add(lblTitulo, java.awt.BorderLayout.NORTH);

        pnlMenu.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlMenu.setLayout(new java.awt.BorderLayout());

        pnlBotones.setLayout(new java.awt.GridLayout(0, 1, 0, 6));

        btnClientes.setText("Clientes");
        pnlBotones.add(btnClientes);

        btnProductos.setText("Productos");
        pnlBotones.add(btnProductos);

        btnInventario.setText("Inventario");
        pnlBotones.add(btnInventario);

        btnPedidos.setText("Pedidos");
        pnlBotones.add(btnPedidos);

        btnCobro.setText("Cobro");
        pnlBotones.add(btnCobro);

        btnFidelizacion.setText("Fidelización");
        pnlBotones.add(btnFidelizacion);

        btnReportes.setText("Reportes");
        pnlBotones.add(btnReportes);

        btnSalir.setText("Salir");
        pnlBotones.add(btnSalir);

        pnlMenu.add(pnlBotones, java.awt.BorderLayout.NORTH);

        getContentPane().add(pnlMenu, java.awt.BorderLayout.WEST);

        pnlContenido.setPreferredSize(new java.awt.Dimension(900, 600));
        pnlContenido.setLayout(new java.awt.CardLayout());

        lblBienvenida.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        lblBienvenida.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBienvenida.setText("Seleccione un módulo del menú");
        pnlContenido.add(lblBienvenida, "inicio");

        getContentPane().add(pnlContenido, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClientes;
    private javax.swing.JButton btnCobro;
    private javax.swing.JButton btnFidelizacion;
    private javax.swing.JButton btnInventario;
    private javax.swing.JButton btnPedidos;
    private javax.swing.JButton btnProductos;
    private javax.swing.JButton btnReportes;
    private javax.swing.JButton btnSalir;
    private javax.swing.JLabel lblBienvenida;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlBotones;
    private javax.swing.JPanel pnlContenido;
    private javax.swing.JPanel pnlMenu;
    // End of variables declaration//GEN-END:variables
}
