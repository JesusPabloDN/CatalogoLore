package com.catalogos;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

/**
 * Módulo de gestión de categorías.
 * Permite listar, agregar, editar y eliminar categorías.
 */
public class VistaCategorias {

    private final Stage              stage;
    private final InterfazPrincipal  hub;
    private final GestorCategoria    gestor;
    private final VBox               root;

    private TableView<Categoria> tabla;
    private TextField            txtNombre;
    private Label                lblMensaje;

    public VistaCategorias(Stage stage, InterfazPrincipal hub) throws SQLException {
        this.stage   = stage;
        this.hub     = hub;
        this.gestor  = new GestorCategoria();
        this.root    = construir();
        cargarTabla();
    }

    public Parent getRoot() { return root; }

    private VBox construir() {
        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(25));

        // Encabezado con botón regresar
        HBox encabezado = encabezado("CATEGORÍAS");

        // Tabla de categorías
        tabla = new TableView<>();
        TableColumn<Categoria, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        colNombre.prefWidthProperty().bind(tabla.widthProperty().multiply(0.95));
        tabla.getColumns().add(colNombre);
        tabla.setPrefHeight(300);

        // Formulario de alta/edición
        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre de la categoría (máx. 30 chars)");
        txtNombre.setId("txt-nombre-cat");

        lblMensaje = new Label();
        lblMensaje.getStyleClass().add("error");

        Button btnAgregar  = new Button("Agregar");
        Button btnEditar   = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("button-peligro");
        btnAgregar.setId("btn-agregar-cat");
        btnEditar.setId("btn-editar-cat");
        btnEliminar.setId("btn-eliminar-cat");

        btnAgregar.setOnAction(e  -> accionAgregar());
        btnEditar.setOnAction(e   -> accionEditar());
        btnEliminar.setOnAction(e -> accionEliminar());

        // Al seleccionar una fila, pre-rellena el campo de texto
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) txtNombre.setText(nuevo.getNombre());
        });

        HBox botones = new HBox(10, btnAgregar, btnEditar, btnEliminar);
        botones.setAlignment(Pos.CENTER_LEFT);

        VBox formulario = new VBox(8, new Label("Nombre:"), txtNombre, botones, lblMensaje);
        formulario.getStyleClass().add("panel-formulario");

        contenedor.getChildren().addAll(encabezado, tabla, formulario);
        return contenedor;
    }

    // Acciones
    private void accionAgregar() {
        try {
            gestor.agregar(txtNombre.getText());
            txtNombre.clear();
            mostrar("Categoría agregada.", false);
            cargarTabla();
        } catch (Exception ex) {
            mostrar(ex.getMessage(), true);
        }
    }

    private void accionEditar() {
        Categoria sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrar("Selecciona una categoría.", true); return; }
        try {
            gestor.actualizar(sel.getIdCategoria(), txtNombre.getText());
            mostrar("Categoría actualizada.", false);
            cargarTabla();
        } catch (Exception ex) {
            mostrar(ex.getMessage(), true);
        }
    }

    private void accionEliminar() {
        Categoria sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrar("Selecciona una categoría.", true); return; }
        try {
            gestor.eliminar(sel.getIdCategoria());
            txtNombre.clear();
            mostrar("Categoría eliminada.", false);
            cargarTabla();
        } catch (Exception ex) {
            mostrar("No se puede eliminar: tiene productos asociados.", true);
        }
    }

    private void cargarTabla() throws SQLException {
        List<Categoria> lista = gestor.listar();
        tabla.setItems(FXCollections.observableArrayList(lista));
    }

    private void mostrar(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #FF2600;" : "-fx-text-fill: #007700;");
    }

    private HBox encabezado(String tituloTexto) {
        Label titulo = new Label(tituloTexto);
        titulo.getStyleClass().add("subtitulo");
        Button btnVolver = new Button("← Menú");
        btnVolver.setId("btn-volver-cat");
        btnVolver.setOnAction(e -> stage.getScene().setRoot(hub.getRoot()));
        HBox hb = new HBox(20, btnVolver, titulo);
        hb.setAlignment(Pos.CENTER_LEFT);
        return hb;
    }
}
