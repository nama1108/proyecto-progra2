package com.panpuntos.vista;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public final class Dialogos {

    private Dialogos() {
    }

    public static void mostrarTexto(Component padre, String titulo, String texto) {
        JTextArea area = new JTextArea(texto);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(480, 420));
        JOptionPane.showMessageDialog(padre, scroll, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    public static void mostrarTabla(Component padre, String titulo, Object[] columnas, List<Object[]> filas) {
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }
        JScrollPane scroll = new JScrollPane(new JTable(modelo));
        scroll.setPreferredSize(new Dimension(700, 300));
        JOptionPane.showMessageDialog(padre, scroll, titulo, JOptionPane.PLAIN_MESSAGE);
    }
}
