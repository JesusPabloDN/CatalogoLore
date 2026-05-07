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

/**
 * Singleton que gestiona la única conexión a la base de datos SQLite.
 * Al inicializarse: crea el directorio, conecta con el archivo .db,
 * activa llaves foráneas y ejecuta el DDL para crear las tablas si no existen.
 */
public class ConexionBD {

    private static ConexionBD instancia;
    private Connection conexion;

    // Ruta donde se almacenará el archivo .db en el equipo del usuario
    private static final String DIRECTORIO_APP =
            System.getProperty("user.home") + "/AppData/Local/CatalogosArtesanales/";
    private static final String NOMBRE_BD = "catalogo_artesanal.db";

    // Constructor privado: flujo de inicialización completo
    private ConexionBD() throws SQLException {
        crearDirectorioSiNoExiste();
        conectar();
        activarLlavesForaneas();
        ejecutarDDL();
    }

    /** Devuelve la instancia única; la crea si aún no existe o si la conexión se cerró. */
    public static ConexionBD getInstance() throws SQLException {
        if (instancia == null || instancia.conexion.isClosed()) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    /** Retorna la conexión activa para uso exclusivo de los DAO. */
    public Connection getConexion() {
        return conexion;
    }

    // -------------------------------------------------------------------------

    /** Crea el directorio de datos de la aplicación si todavía no existe. */
    private void crearDirectorioSiNoExiste() {
        try {
            Files.createDirectories(Paths.get(DIRECTORIO_APP));
        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo crear el directorio de la BD: " + DIRECTORIO_APP, e);
        }
    }

    /** Abre la conexión JDBC con el archivo SQLite en la ruta de la aplicación. */
    private void conectar() throws SQLException {
        String url = "jdbc:sqlite:" + DIRECTORIO_APP + NOMBRE_BD;
        conexion = DriverManager.getConnection(url);
    }

    /** Activa la verificación de integridad referencial (desactivada por defecto en SQLite). */
    private void activarLlavesForaneas() throws SQLException {
        try (Statement stmt = conexion.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
    }

    /**
     * Lee el script DDL desde el classpath y lo ejecuta sentencia por sentencia.
     * Gracias al IF NOT EXISTS, es seguro llamarlo en cada inicio de la aplicación.
     */
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

    /** Cierra la conexión al apagar la aplicación. Llamar desde Main.stop(). */
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
