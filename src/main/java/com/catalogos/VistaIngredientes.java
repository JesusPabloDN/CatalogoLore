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
 * Módulo de gestión de ingredientes (insumos por producto).
 * Permite asociar insumos a productos con su cantidad necesaria por unidad fabricada.
 */
public class VistaIngredientes {

    private final Stage              stage;
    private final InterfazPrincipal  hub;
    private final GestorIngrediente  gestor;
    private final GestorProducto     gestorProd;
    private final GestorInsumo       gestorInsumo;
    private final VBox               root;

    private ComboBox<Producto>         cbProducto;
    private ComboBox<Insumo>           cbInsumo;
    private TextField                  txtCantidad;
    private TableView<ProductoInsumo>  tabla;
    private Label                      lblMensaje;

    public VistaIngredientes(Stage stage, InterfazPrincipal hub) throws SQLException {
        this.stage        = stage;
        this.hub          = hub;
        this.gestor       = new GestorIngrediente();
        this.gestorProd   = new GestorProducto();
        this.gestorInsumo = new GestorInsumo();
        this.root         = construir();
        cargarCombos();
    }

    public Parent getRoot() { return root; }

    private VBox construir() {
        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(25));

        HBox encabezado = encabezado("INGREDIENTES");

        cbProducto = new ComboBox<>();
        cbProducto.setId("cb-producto-ing");
        cbProducto.setPromptText("Selecciona producto...");
        cbProducto.setMaxWidth(Double.MAX_VALUE);

        // Al cambiar producto, carga su lista de ingredientes
        cbProducto.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                try { cargarTabla(sel.getIdProducto()); }
                catch (SQLException ignored) {}
            }
        });

        cbInsumo = new ComboBox<>();
        cbInsumo.setId("cb-insumo-ing");
        cbInsumo.setPromptText("Insumo...");
        cbInsumo.setMaxWidth(Double.MAX_VALUE);

        txtCantidad = new TextField();
        txtCantidad.setId("txt-cantidad-ing");
        txtCantidad.setPromptText("Cantidad necesaria por unidad");

        // Tabla de ingredientes del producto seleccionado
        tabla = new TableView<>();
        TableColumn<ProductoInsumo, String> colInsumo = new TableColumn<>("Insumo ID");
        colInsumo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(c.getValue().getFkInsumo())));
        TableColumn<ProductoInsumo, String> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(c.getValue().getCantidadNecesaria())));
        tabla.getColumns().addAll(colInsumo, colCant);
        tabla.setPrefHeight(200);

        lblMensaje = new Label();
        lblMensaje.getStyleClass().add("error");

        Button btnAgregar  = new Button("Agregar ingrediente");
        Button btnEliminar = new Button("Eliminar ingrediente");
        btnEliminar.getStyleClass().add("button-peligro");
        btnAgregar.setId("btn-agregar-ing");
        btnEliminar.setId("btn-eliminar-ing");

        btnAgregar.setOnAction(e  -> accionAgregar());
        btnEliminar.setOnAction(e -> accionEliminar());

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8);
        form.getStyleClass().add("panel-formulario");
        form.addRow(0, new Label("Producto:"), cbProducto);
        form.addRow(1, new Label("Insumo:"),   cbInsumo);
        form.addRow(2, new Label("Cantidad:"), txtCantidad);
        form.add(new HBox(10, btnAgregar, btnEliminar), 0, 3, 2, 1);
        form.add(lblMensaje, 0, 4, 2, 1);

        contenedor.getChildren().addAll(encabezado, form,
                new Label("Ingredientes del producto seleccionado:"), tabla);
        return contenedor;
    }

    private void accionAgregar() {
        Producto p = cbProducto.getSelectionModel().getSelectedItem();
        Insumo   i = cbInsumo.getSelectionModel().getSelectedItem();
        if (p == null || i == null) { mostrar("Selecciona producto e insumo.", true); return; }
        try {
            double cantidad = Double.parseDouble(txtCantidad.getText().trim());
            gestor.agregar(p.getIdProducto(), i.getIdInsumo(), cantidad);
            mostrar("Ingrediente agregado.", false);
            txtCantidad.clear();
            cargarTabla(p.getIdProducto());
        } catch (NumberFormatException e) {
            mostrar("Cantidad inválida.", true);
        } catch (Exception ex) {
            mostrar(ex.getMessage(), true);
        }
    }

    private void accionEliminar() {
        Producto p = cbProducto.getSelectionModel().getSelectedItem();
        ProductoInsumo sel = tabla.getSelectionModel().getSelectedItem();
        if (p == null || sel == null) { mostrar("Selecciona producto e ingrediente.", true); return; }
        try {
            gestor.eliminar(p.getIdProducto(), sel.getFkInsumo());
            mostrar("Ingrediente eliminado.", false);
            cargarTabla(p.getIdProducto());
        } catch (Exception ex) {
            mostrar(ex.getMessage(), true);
        }
    }

    private void cargarCombos() throws SQLException {
        cbProducto.setItems(FXCollections.observableArrayList(gestorProd.listarTodos()));
        cbInsumo.setItems(FXCollections.observableArrayList(gestorInsumo.listarTodos()));
    }

    private void cargarTabla(int idProducto) throws SQLException {
        List<ProductoInsumo> lista = gestor.listarPorProducto(idProducto);
        tabla.setItems(FXCollections.observableArrayList(lista));
    }

    private void mostrar(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill:#FF2600;" : "-fx-text-fill:#007700;");
    }

    private HBox encabezado(String txt) {
        Label lbl = new Label(txt); lbl.getStyleClass().add("subtitulo");
        Button btn = new Button("← Menú");
        btn.setId("btn-volver-ing");
        btn.setOnAction(e -> stage.getScene().setRoot(hub.getRoot()));
        HBox hb = new HBox(20, btn, lbl); hb.setAlignment(Pos.CENTER_LEFT);
        return hb;
    }
}
