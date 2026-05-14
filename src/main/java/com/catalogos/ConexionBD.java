package com.catalogos;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Clase que controla la conexion a la base de datos de manera unica
public class ConexionBD {

    private static ConexionBD instancia;
    private Connection conexion;

    // Indica la ruta donde se guardara la base de datos
    private static final String DIRECTORIO_APP =
            System.getProperty("user.home") + "/AppData/Local/CatalogosArtesanales/";
    private static final String NOMBRE_BD = "catalogo_artesanal.db";

    // Inicia la conexion y crea las tablas
    private ConexionBD() throws SQLException {
        crearDirectorioSiNoExiste();
        conectar();
        activarLlavesForaneas();
        ejecutarDDL();
    }

    // Entrega la conexion activa de la base de datos
    public static ConexionBD getInstance() throws SQLException {
        if (instancia == null || instancia.conexion.isClosed()) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    // Devuelve la conexion actual
    public Connection getConexion() {
        return conexion;
    }

    // -------------------------------------------------------------------------

    // Crea la carpeta para guardar la base de datos si no existe
    private void crearDirectorioSiNoExiste() {
        try {
            Files.createDirectories(Paths.get(DIRECTORIO_APP));
        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo crear el directorio de la BD: " + DIRECTORIO_APP, e);
        }
    }

    // Se conecta con el archivo de la base de datos
    private void conectar() throws SQLException {
        String url = "jdbc:sqlite:" + DIRECTORIO_APP + NOMBRE_BD;
        conexion = DriverManager.getConnection(url);
    }

    // Activa la revision de llaves foraneas en la base de datos
    private void activarLlavesForaneas() throws SQLException {
        try (Statement stmt = conexion.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
    }

    // Ejecuta el archivo que crea todas las tablas de la base de datos
    private void ejecutarDDL() throws SQLException {
        try (InputStream is = getClass().getResourceAsStream("/db/catalogo_lore_ddl.sql")) {

            if (is == null) {
                throw new RuntimeException(
                        "No se encontró el DDL en el classpath: /db/catalogo_lore_ddl.sql");
            }

            // Lee el archivo completo y elimina líneas de solo comentario
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            StringBuilder limpio = new StringBuilder();
            for (String linea : sql.split("\n")) {
                String t = linea.trim();
                if (!t.startsWith("--") && !t.isEmpty()) {
                    limpio.append(linea).append("\n");
                }
            }

            // Ejecuta cada sentencia SQL por separado (split por ';')
            try (Statement stmt = conexion.createStatement()) {
                for (String sentencia : limpio.toString().split(";")) {
                    String s = sentencia.strip();
                    if (!s.isEmpty()) {
                        stmt.execute(s);
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error al leer el script DDL", e);
        }
    }

    // Cierra la base de datos cuando se cierra el programa
    public void cerrar() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
