package com.catalogos;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Módulo de gestión de insumos (materia prima).
 * Permite listar, agregar, editar y eliminar insumos del inventario.
 */
public class VistaInsumos {

    private final Stage             stage;
    private final InterfazPrincipal hub;
    private final GestorInsumo      gestor;
    private final VBox              root;

    private TableView<Insumo> tabla;
    private TextField txtNombre, txtStock, txtUnidad;
    private Label     lblMensaje;

    public VistaInsumos(Stage stage, InterfazPrincipal hub) throws SQLException {
        this.stage  = stage;
        this.hub    = hub;
        this.gestor = new GestorInsumo();
        this.root   = construir();
        cargarTabla();
    }

    public Parent getRoot() { return root; }

    private VBox construir() {
        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(25));

        HBox encabezado = encabezado("INSUMOS");

        tabla = new TableView<>();
        agregarColumna("Nombre",  i -> i.getNombre());
        agregarColumna("Stock",   i -> String.valueOf(i.getStockActual()));
        agregarColumna("Unidad",  i -> i.getUnidadMedida());
        tabla.setPrefHeight(270);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                txtNombre.setText(sel.getNombre());
                txtStock.setText(String.valueOf(sel.getStockActual()));
                txtUnidad.setText(sel.getUnidadMedida());
            }
        });

        txtNombre = new TextField(); txtNombre.setId("txt-nombre-insumo"); txtNombre.setPromptText("Nombre");
        txtStock  = new TextField(); txtStock.setId("txt-stock-insumo");   txtStock.setPromptText("Stock inicial");
        txtUnidad = new TextField(); txtUnidad.setId("txt-unidad-insumo"); txtUnidad.setPromptText("Unidad (ml, gr, piezas...)");

        lblMensaje = new Label(); lblMensaje.getStyleClass().add("error");

        Button btnAgregar  = new Button("Agregar");
        Button btnEditar   = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("button-peligro");
        btnAgregar.setId("btn-agregar-insumo");
        btnEditar.setId("btn-editar-insumo");
        btnEliminar.setId("btn-eliminar-insumo");

        btnAgregar.setOnAction(e  -> accionAgregar());
        btnEditar.setOnAction(e   -> accionEditar());
        btnEliminar.setOnAction(e -> accionEliminar());

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8);
        form.getStyleClass().add("panel-formulario");
        form.addRow(0, new Label("Nombre:"),  txtNombre);
        form.addRow(1, new Label("Stock:"),   txtStock);
        form.addRow(2, new Label("Unidad:"),  txtUnidad);
        form.add(new HBox(10, btnAgregar, btnEditar, btnEliminar), 0, 3, 2, 1);
        form.add(lblMensaje, 0, 4, 2, 1);

        contenedor.getChildren().addAll(encabezado, tabla, form);
        return contenedor;
    }

    private void accionAgregar() {
        try {
            double stock = Double.parseDouble(txtStock.getText().trim());
            gestor.agregar(txtNombre.getText(), stock, txtUnidad.getText());
            mostrar("Insumo agregado.", false); limpiar(); cargarTabla();
        } catch (NumberFormatException e) { mostrar("Stock inválido.", true); }
        catch (Exception ex) { mostrar(ex.getMessage(), true); }
    }

    private void accionEditar() {
        Insumo sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrar("Selecciona un insumo.", true); return; }
        try {
            double stock = Double.parseDouble(txtStock.getText().trim());
            sel.setNombre(txtNombre.getText().trim());
            sel.setStockActual(stock);
            sel.setUnidadMedida(txtUnidad.getText().trim());
            gestor.actualizar(sel);
            mostrar("Insumo actualizado.", false); cargarTabla();
        } catch (NumberFormatException e) { mostrar("Stock inválido.", true); }
        catch (Exception ex) { mostrar(ex.getMessage(), true); }
    }

    private void accionEliminar() {
        Insumo sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrar("Selecciona un insumo.", true); return; }
        try {
            gestor.eliminar(sel.getIdInsumo());
            mostrar("Insumo eliminado.", false); limpiar(); cargarTabla();
        } catch (Exception ex) { mostrar("Error al eliminar: " + ex.getMessage(), true); }
    }

    private void cargarTabla() {
        try { tabla.setItems(FXCollections.observableArrayList(gestor.listarTodos())); }
        catch (SQLException ignored) {}
    }

    private void limpiar() { txtNombre.clear(); txtStock.clear(); txtUnidad.clear(); }

    private void mostrar(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill:#FF2600;" : "-fx-text-fill:#007700;");
    }

    @SuppressWarnings("unchecked")
    private void agregarColumna(String titulo, java.util.function.Function<Insumo, String> fn) {
        TableColumn<Insumo, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(fn.apply(c.getValue())));
        tabla.getColumns().add(col);
    }

    private HBox encabezado(String txt) {
        Label lbl = new Label(txt); lbl.getStyleClass().add("subtitulo");
        Button btn = new Button("← Menú");
        btn.setId("btn-volver-insumos");
        btn.setOnAction(e -> stage.getScene().setRoot(hub.getRoot()));
        HBox hb = new HBox(20, btn, lbl); hb.setAlignment(Pos.CENTER_LEFT);
        return hb;
    }
}
