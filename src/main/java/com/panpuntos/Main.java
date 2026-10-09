package com.panpuntos;

import com.panpuntos.conexion.ConexionBD;
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
        SwingUtilities.invokeLater(Main::probarConexion);
    }

    private static void probarConexion() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            JOptionPane.showMessageDialog(null,
                    "Conexión exitosa a " + conexion.getMetaData().getDatabaseProductVersion(),
                    "Pan, Puntos y Premios", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error de conexión: " + e.getMessage(),
                    "Pan, Puntos y Premios", JOptionPane.ERROR_MESSAGE);
        }
    }
}
