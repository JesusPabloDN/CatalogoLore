package com.catalogos;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

// Archivo principal que arranca el programa
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Muestra la ventana del menu principal
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
        // Desconecta la base de datos cuando se cierra el programa
        try {
            ConexionBD.getInstance().cerrar();
        } catch (SQLException e) {
            System.err.println("Error al cerrar la BD: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Prepara la base de datos antes de abrir la pantalla
        try {
            ConexionBD.getInstance();
        } catch (SQLException e) {
            System.err.println("Error crítico al iniciar la BD: " + e.getMessage());
            System.exit(1);
        }
        launch(args);
    }
}
