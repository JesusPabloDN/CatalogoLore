package com.catalogos;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Pantalla del menu principal donde estan todos los botones
public class InterfazPrincipal {

    private final VBox    root;
    private final Stage   stage;

    // Espacio entre cada boton
    private static final double GAP = 15;

    public InterfazPrincipal(Stage stage) {
        this.stage = stage;
        this.root  = construir();
    }

    public Parent getRoot() { return root; }

    private VBox construir() {
        VBox contenedor = new VBox(20);
        contenedor.setAlignment(Pos.TOP_CENTER);
        contenedor.setPadding(new Insets(30));

        // Titulo de arriba
        Label titulo = new Label("CATÁLOGOS DIGITALES");
        titulo.getStyleClass().add("titulo");

        Label subtitulo = new Label("Selecciona un módulo");
        subtitulo.getStyleClass().add("subtitulo");

        // Primera fila de botones
        HBox fila0 = filaBtn(
                crearBotonModulo("Categorías",    "Categorias"),
                crearBotonModulo("Productos",     "Productos"),
                crearBotonModulo("Stock",         "Stock")
        );

        // Segunda fila de botones
        HBox fila1 = filaBtn(
                crearBotonModulo("Ingredientes",  "Ingredientes"),
                crearBotonModulo("Insumos",       "Insumos")
        );

        // Deja un espacio mas grande para separar las secciones
        Region separador = new Region();
        separador.setPrefHeight(GAP);   // GAP extra además del spacing del VBox (=GAP)
        // Ajuste de espacio vacio

        // Tercera fila de botones
        HBox fila2 = filaBtn(
                crearBotonModulo("Clientes",      "Clientes"),
                crearBotonModulo("Pedidos",       "Pedidos")
        );

        // Cuarta fila de botones
        HBox fila3 = filaBtn(
                crearBotonModulo("Datos Catálogo","DatosCatalogo"),
                crearBotonModulo("Generar PDF",   "GenerarPDF")
        );

        contenedor.getChildren().addAll(
                titulo, subtitulo,
                fila0,
                fila1,
                separador,   // separacion extra
                fila2,
                fila3
        );
        return contenedor;
    }

    // Acomoda los botones en linea
    private HBox filaBtn(Button... botones) {
        HBox fila = new HBox(GAP, botones);
        fila.setAlignment(Pos.CENTER);
        return fila;
    }

    // Crea un boton para ir a otra pantalla
    private Button crearBotonModulo(String etiqueta, String nombreModulo) {
        Button btn = new Button(etiqueta);
        btn.getStyleClass().add("boton-menu");
        btn.setId("btn-" + nombreModulo.toLowerCase());
        btn.setOnAction(e -> navegarA(nombreModulo));
        return btn;
    }

    // Cambia la pantalla actual por la que se eligio
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

    // Muestra un error si falla al abrir la pantalla
    private void mostrarError(String mensaje) {
        System.err.println(mensaje);
    }
}
