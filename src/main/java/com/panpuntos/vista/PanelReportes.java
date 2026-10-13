package com.panpuntos.vista;

import com.panpuntos.dto.ResultadoReporte;
import com.panpuntos.excepcion.ValidacionException;
import com.panpuntos.servicio.ReporteService;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class PanelReportes extends javax.swing.JPanel {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReporteService reporteService = new ReporteService();

    public PanelReportes() {
        initComponents();
        cmbReporte.setModel(new DefaultComboBoxModel<>(ReporteService.REPORTES));
        LocalDate hoy = LocalDate.now();
        txtDesde.setText(hoy.withDayOfMonth(1).format(FORMATO_FECHA));
        txtHasta.setText(hoy.format(FORMATO_FECHA));
        configurarEventos();
        actualizarFiltros();
    }

    private void configurarEventos() {
        cmbReporte.addActionListener(e -> {
            actualizarFiltros();
            generar();
        });
        btnGenerar.addActionListener(e -> generar());
    }

    private void actualizarFiltros() {
        boolean conFechas = cmbReporte.getSelectedIndex() == 0;
        txtDesde.setEnabled(conFechas);
        txtHasta.setEnabled(conFechas);
    }

    public void generar() {
        try {
            int indice = cmbReporte.getSelectedIndex();
            LocalDate desde = indice == 0 ? leerFecha(txtDesde.getText(), "inicial") : null;
            LocalDate hasta = indice == 0 ? leerFecha(txtHasta.getText(), "final") : null;
            mostrar(reporteService.generar(indice, desde, hasta));
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate leerFecha(String texto, String cual) throws ValidacionException {
        try {
            return LocalDate.parse(texto.trim(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new ValidacionException("La fecha " + cual + " no es válida. Use el formato dd/mm/aaaa (ej. 01/10/2026)");
        }
    }

    private void mostrar(ResultadoReporte reporte) {
        DefaultTableModel modelo = new DefaultTableModel(reporte.getColumnas(), 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        for (Object[] fila : reporte.getFilas()) {
            modelo.addRow(fila);
        }
        tblResultado.setModel(modelo);
        pnlResultado.setBorder(javax.swing.BorderFactory.createTitledBorder(reporte.getTitulo()));
        lblResumen.setText(reporte.getFilas().isEmpty() ? "Sin datos para mostrar" : reporte.getResumen());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFiltros = new javax.swing.JPanel();
        lblReporte = new javax.swing.JLabel();
        cmbReporte = new javax.swing.JComboBox<>();
        lblDesde = new javax.swing.JLabel();
        txtDesde = new javax.swing.JTextField();
        lblHasta = new javax.swing.JLabel();
        txtHasta = new javax.swing.JTextField();
        btnGenerar = new javax.swing.JButton();
        pnlResultado = new javax.swing.JPanel();
        scrResultado = new javax.swing.JScrollPane();
        tblResultado = new javax.swing.JTable();
        lblResumen = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new java.awt.BorderLayout());

        pnlFiltros.setBorder(javax.swing.BorderFactory.createTitledBorder("Seleccione el reporte"));
        pnlFiltros.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblReporte.setText("Reporte:");
        pnlFiltros.add(lblReporte);
        pnlFiltros.add(cmbReporte);

        lblDesde.setText("Desde (dd/mm/aaaa):");
        pnlFiltros.add(lblDesde);

        txtDesde.setColumns(10);
        pnlFiltros.add(txtDesde);

        lblHasta.setText("Hasta:");
        pnlFiltros.add(lblHasta);

        txtHasta.setColumns(10);
        pnlFiltros.add(txtHasta);

        btnGenerar.setText("Generar");
        pnlFiltros.add(btnGenerar);

        add(pnlFiltros, java.awt.BorderLayout.NORTH);

        pnlResultado.setBorder(javax.swing.BorderFactory.createTitledBorder("Resultado"));
        pnlResultado.setLayout(new java.awt.BorderLayout());

        scrResultado.setViewportView(tblResultado);

        pnlResultado.add(scrResultado, java.awt.BorderLayout.CENTER);

        lblResumen.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        lblResumen.setText(" ");
        lblResumen.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 4, 4, 4));
        pnlResultado.add(lblResumen, java.awt.BorderLayout.SOUTH);

        add(pnlResultado, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGenerar;
    private javax.swing.JComboBox<String> cmbReporte;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblReporte;
    private javax.swing.JLabel lblResumen;
    private javax.swing.JPanel pnlFiltros;
    private javax.swing.JPanel pnlResultado;
    private javax.swing.JScrollPane scrResultado;
    private javax.swing.JTable tblResultado;
    private javax.swing.JTextField txtDesde;
    private javax.swing.JTextField txtHasta;
    // End of variables declaration//GEN-END:variables
}
