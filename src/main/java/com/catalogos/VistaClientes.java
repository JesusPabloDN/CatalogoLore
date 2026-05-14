package com.catalogos;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;

// Pantalla para administrar a los clientes
public class VistaClientes {

    private final Stage             stage;
    private final InterfazPrincipal hub;
    private final GestorCliente     gestor;
    private final VBox              root;

    private TableView<Cliente> tabla;
    private TextField txtNombre, txtTelefono, txtDireccion;
    private Label     lblMensaje;

    public VistaClientes(Stage stage, InterfazPrincipal hub) throws SQLException {
        this.stage  = stage;
        this.hub    = hub;
        this.gestor = new GestorCliente();
        this.root   = construir();
        cargarTabla();
    }

    public Parent getRoot() { return root; }

    private VBox construir() {
        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(25));

        HBox encabezado = encabezado("CLIENTES");

        tabla = new TableView<>();

        // Columna del nombre
        TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreCompleto()));
        colNombre.setPrefWidth(200);
        colNombre.setMinWidth(150);
        colNombre.setResizable(true);

        // Columna del telefono
        TableColumn<Cliente, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getTelefono()));
        colTel.setPrefWidth(120);
        colTel.setMinWidth(100);
        colTel.setResizable(false);

        // Columna de la direccion
        TableColumn<Cliente, String> colDir = new TableColumn<>("Dirección");
        colDir.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getDireccionEntrega() != null
                        ? c.getValue().getDireccionEntrega() : ""));
        colDir.setMaxWidth(Double.MAX_VALUE);
        colDir.setResizable(true);

        tabla.getColumns().add(colNombre);
        tabla.getColumns().add(colTel);
        tabla.getColumns().add(colDir);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPrefHeight(270);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                txtNombre.setText(sel.getNombreCompleto());
                txtTelefono.setText(sel.getTelefono());
                txtDireccion.setText(sel.getDireccionEntrega() != null ? sel.getDireccionEntrega() : "");
            }
        });

        txtNombre    = new TextField(); txtNombre.setId("txt-nombre-cli");    txtNombre.setPromptText("Nombre completo");
        txtTelefono  = new TextField(); txtTelefono.setId("txt-tel-cli");     txtTelefono.setPromptText("10 dígitos");
        txtDireccion = new TextField(); txtDireccion.setId("txt-dir-cli");    txtDireccion.setPromptText("Dirección de entrega (opcional)");

        lblMensaje = new Label(); lblMensaje.getStyleClass().add("error");

        Button btnAgregar  = new Button("Agregar");
        Button btnEditar   = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("button-peligro");
        btnAgregar.setId("btn-agregar-cli");
        btnEditar.setId("btn-editar-cli");
        btnEliminar.setId("btn-eliminar-cli");

        btnAgregar.setOnAction(e  -> accionAgregar());
        btnEditar.setOnAction(e   -> accionEditar());
        btnEliminar.setOnAction(e -> accionEliminar());

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8);
        form.getStyleClass().add("panel-formulario");
        form.addRow(0, new Label("Nombre:"),    txtNombre);
        form.addRow(1, new Label("Teléfono:"),  txtTelefono);
        form.addRow(2, new Label("Dirección:"), txtDireccion);
        form.add(new HBox(10, btnAgregar, btnEditar, btnEliminar), 0, 3, 2, 1);
        form.add(lblMensaje, 0, 4, 2, 1);

        contenedor.getChildren().addAll(encabezado, tabla, form);
        return contenedor;
    }

    private void accionAgregar() {
        try {
            gestor.agregar(txtNombre.getText(), txtTelefono.getText(), txtDireccion.getText());
            mostrar("Cliente registrado.", false); limpiar(); cargarTabla();
        } catch (Exception ex) { mostrar(ex.getMessage(), true); }
    }

    private void accionEditar() {
        Cliente sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrar("Selecciona un cliente.", true); return; }
        try {
            sel.setNombreCompleto(txtNombre.getText().trim());
            sel.setTelefono(txtTelefono.getText().trim());
            sel.setDireccionEntrega(txtDireccion.getText().trim());
            gestor.actualizar(sel);
            mostrar("Cliente actualizado.", false); cargarTabla();
        } catch (Exception ex) { mostrar(ex.getMessage(), true); }
    }

    private void accionEliminar() {
        Cliente sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrar("Selecciona un cliente.", true); return; }
        try {
            gestor.eliminar(sel.getIdCliente());
            mostrar("Cliente eliminado.", false); limpiar(); cargarTabla();
        } catch (Exception ex) { mostrar("No se puede eliminar: tiene pedidos asociados.", true); }
    }

    private void cargarTabla() {
        try { tabla.setItems(FXCollections.observableArrayList(gestor.listarTodos())); }
        catch (SQLException ignored) {}
    }

    private void limpiar() { txtNombre.clear(); txtTelefono.clear(); txtDireccion.clear(); }

    private void mostrar(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill:#FF2600;" : "-fx-text-fill:#007700;");
    }


    private HBox encabezado(String txt) {
        Label lbl = new Label(txt); lbl.getStyleClass().add("subtitulo");
        Button btn = new Button("← Menú");
        btn.setId("btn-volver-cli");
        btn.setOnAction(e -> stage.getScene().setRoot(hub.getRoot()));
        HBox hb = new HBox(20, btn, lbl); hb.setAlignment(Pos.CENTER_LEFT);
        return hb;
    }
}
