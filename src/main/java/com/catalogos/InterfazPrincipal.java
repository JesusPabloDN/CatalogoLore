package com.catalogos;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Pantalla central de la aplicación (flujo en estrella).
 * Cada botón abre el módulo correspondiente en la misma ventana.
 */
public class InterfazPrincipal {

    private final VBox    root;
    private final Stage   stage;

    // Módulos del sistema
    private static final String[][] MODULOS = {
        {"Categorías",       "Categorias"},
        {"Productos",        "Productos"},
        {"Stock",            "Stock"},
        {"Ingredientes",     "Ingredientes"},
        {"Datos Catálogo",   "DatosCatalogo"},
        {"Insumos",          "Insumos"},
        {"Clientes",         "Clientes"},
        {"Pedidos",          "Pedidos"},
        {"Generar PDF",      "GenerarPDF"}
    };

    public InterfazPrincipal(Stage stage) {
        this.stage = stage;
        this.root  = construir();
    }

    public Parent getRoot() { return root; }

    private VBox construir() {
        VBox contenedor = new VBox(20);
        contenedor.setAlignment(Pos.TOP_CENTER);
        contenedor.setPadding(new Insets(30));

        // Título principal
        Label titulo = new Label("CATÁLOGOS DIGITALES");
        titulo.getStyleClass().add("titulo");

        Label subtitulo = new Label("Selecciona un módulo");
        subtitulo.getStyleClass().add("subtitulo");

        // Cuadrícula 3×3 de botones de módulo
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        int col = 0, row = 0;
        for (String[] modulo : MODULOS) {
            Button btn = crearBotonModulo(modulo[0], modulo[1]);
            grid.add(btn, col, row);
            col++;
            if (col == 3) { col = 0; row++; }
        }

        contenedor.getChildren().addAll(titulo, subtitulo, grid);
        return contenedor;
    }

    /** Crea un botón del menú principal que navega al módulo indicado. */
    private Button crearBotonModulo(String etiqueta, String nombreModulo) {
        Button btn = new Button(etiqueta);
        btn.getStyleClass().add("boton-menu");
        btn.setId("btn-" + nombreModulo.toLowerCase());
        btn.setOnAction(e -> navegarA(nombreModulo));
        return btn;
    }

    /** Sustituye el contenido de la ventana por el módulo solicitado. */
    private void navegarA(String modulo) {
        try {
            Parent vista = switch (modulo) {
                case "Categorias"   -> new VistaCategorias(stage, this).getRoot();
                case "Productos"    -> new VistaProductos(stage, this).getRoot();
                case "Stock"        -> new VistaStock(stage, this).getRoot();
                case "Ingredientes" -> new VistaIngredientes(stage, this).getRoot();
                case "DatosCatalogo"-> new VistaDatosCatalogo(stage, this).getRoot();
                case "Insumos"      -> new VistaInsumos(stage, this).getRoot();
                case "Clientes"     -> new VistaClientes(stage, this).getRoot();
                case "Pedidos"      -> new VistaPedidos(stage, this).getRoot();
                case "GenerarPDF"   -> new VistaGenerarPDF(stage, this).getRoot();
                default -> root;
            };
            stage.getScene().setRoot(vista);
        } catch (Exception ex) {
            mostrarError("Error al abrir módulo: " + ex.getMessage());
        }
    }

    /** Muestra un mensaje de error en la consola (se maneja en vistas con Label). */
    private void mostrarError(String mensaje) {
        System.err.println(mensaje);
    }
}
