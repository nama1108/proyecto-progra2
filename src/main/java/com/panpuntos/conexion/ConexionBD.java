package com.panpuntos.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD {

    private static final String ARCHIVO_CONFIGURACION = "/db.properties";
    private static final Properties CONFIGURACION = new Properties();

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        cargarConfiguracion();
        return DriverManager.getConnection(
                CONFIGURACION.getProperty("db.url"),
                CONFIGURACION.getProperty("db.usuario"),
                CONFIGURACION.getProperty("db.clave"));
    }

    private static synchronized void cargarConfiguracion() throws SQLException {
        if (!CONFIGURACION.isEmpty()) {
            return;
        }
        try (InputStream entrada = ConexionBD.class.getResourceAsStream(ARCHIVO_CONFIGURACION)) {
            if (entrada == null) {
                throw new SQLException("No se encontró src/main/resources/db.properties. "
                        + "Copie db.properties.ejemplo y configure sus credenciales.");
            }
            CONFIGURACION.load(entrada);
        } catch (IOException e) {
            throw new SQLException("No se pudo leer db.properties", e);
        }
    }
}
