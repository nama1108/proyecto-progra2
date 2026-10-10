package com.panpuntos;

import com.panpuntos.conexion.ConexionBD;
import com.panpuntos.vista.FrmPrincipal;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el estilo del sistema: " + e.getMessage());
        }
        SwingUtilities.invokeLater(Main::iniciar);
    }

    private static void iniciar() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            new FrmPrincipal().setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a la base de datos: " + e.getMessage()
                    + "\nVerifique que Oracle esté encendido (docker start oracle23).",
                    "Pan, Puntos y Premios", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
