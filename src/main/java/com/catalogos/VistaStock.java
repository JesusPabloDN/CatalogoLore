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
 * Módulo de control de stock terminado.
 * Permite seleccionar un producto e incrementar sus unidades listas.
 */
public class VistaStock {

    private final Stage             stage;
    private final InterfazPrincipal hub;
    private final GestorProducto    gestor;
    private final VBox              root;

    private ComboBox<Producto> cbProducto;
    private Label              lblStockActual;
    private TextField          txtUnidades;
    private Label              lblMensaje;

    public VistaStock(Stage stage, InterfazPrincipal hub) throws SQLException {
        this.stage  = stage;
        this.hub    = hub;
        this.gestor = new GestorProducto();
        this.root   = construir();
        cargarProductos();
    }

    public Parent getRoot() { return root; }

    private VBox construir() {
        VBox contenedor = new VBox(20);
        contenedor.setPadding(new Insets(25));

        HBox encabezado = encabezado("STOCK TERMINADO");

        cbProducto = new ComboBox<>();
        cbProducto.setId("cb-producto-stock");
        cbProducto.setPromptText("Selecciona un producto...");
        cbProducto.setMaxWidth(Double.MAX_VALUE);

        lblStockActual = new Label("Stock actual: —");
        lblStockActual.getStyleClass().add("subtitulo");

        // Al cambiar producto, muestra el stock actual
        cbProducto.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                lblStockActual.setText("Stock actual: " + sel.getStockTerminado() + " unidades");
            }
        });

        txtUnidades = new TextField();
        txtUnidades.setId("txt-unidades-stock");
        txtUnidades.setPromptText("Unidades a agregar");

        lblMensaje = new Label();
        lblMensaje.getStyleClass().add("error");

        Button btnAgregar = new Button("Agregar unidades");
        btnAgregar.setId("btn-agregar-stock");
        btnAgregar.setOnAction(e -> accionAgregarStock());

        VBox panel = new VBox(12,
                new Label("Producto:"), cbProducto,
                lblStockActual,
                new Label("Unidades a agregar:"), txtUnidades,
                btnAgregar, lblMensaje);
        panel.getStyleClass().add("panel-formulario");
        panel.setMaxWidth(500);

        contenedor.getChildren().addAll(encabezado, panel);
        return contenedor;
    }

    private void accionAgregarStock() {
        Producto sel = cbProducto.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrar("Selecciona un producto.", true); return; }
        try {
            int unidades = Integer.parseInt(txtUnidades.getText().trim());
            gestor.agregarStock(sel.getIdProducto(), unidades);
            mostrar("Stock actualizado correctamente.", false);
            txtUnidades.clear();
            cargarProductos();
            // Refresca el label del stock
            cbProducto.getSelectionModel().clearSelection();
            lblStockActual.setText("Stock actual: —");
        } catch (NumberFormatException e) {
            mostrar("Ingresa un número entero válido.", true);
        } catch (Exception ex) {
            mostrar(ex.getMessage(), true);
        }
    }

    private void cargarProductos() throws SQLException {
        cbProducto.setItems(FXCollections.observableArrayList(gestor.listarTodos()));
    }

    private void mostrar(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill:#FF2600;" : "-fx-text-fill:#007700;");
    }

    private HBox encabezado(String txt) {
        Label lbl = new Label(txt); lbl.getStyleClass().add("subtitulo");
        Button btn = new Button("← Menú");
        btn.setId("btn-volver-stock");
        btn.setOnAction(e -> stage.getScene().setRoot(hub.getRoot()));
        HBox hb = new HBox(20, btn, lbl); hb.setAlignment(Pos.CENTER_LEFT);
        return hb;
    }
}
