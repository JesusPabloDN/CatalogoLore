package com.catalogos;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Punto de entrada de la aplicación JavaFX.
 * Inicializa la BD en el arranque y cierra la conexión al salir.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Carga la pantalla principal (flujo en estrella)
        InterfazPrincipal vista = new InterfazPrincipal(stage);
        Scene escena = new Scene(vista.getRoot(), 900, 650);
        escena.getStylesheets().add(
                getClass().getResource("/css/estilo.css").toExternalForm());

        stage.setTitle("Sistema Gestor de Catálogos Digitales");
        stage.setScene(escena);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
        // Cierra la conexión a la BD al cerrar la ventana
        try {
            ConexionBD.getInstance().cerrar();
        } catch (SQLException e) {
            System.err.println("Error al cerrar la BD: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Inicializa la BD antes de lanzar JavaFX
        try {
            ConexionBD.getInstance();
        } catch (SQLException e) {
            System.err.println("Error crítico al iniciar la BD: " + e.getMessage());
            System.exit(1);
        }
        launch(args);
    }
}
