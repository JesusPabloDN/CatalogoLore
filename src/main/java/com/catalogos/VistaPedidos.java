package com.catalogos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Módulo de gestión de pedidos.
 * Permite crear nuevos pedidos, ver el detalle y marcarlos como entregados.
 */
public class VistaPedidos {

    private final Stage             stage;
    private final InterfazPrincipal hub;
    private final GestorPedido      gestor;
    private final GestorCliente     gestorCli;
    private final GestorProducto    gestorProd;
    private final VBox              root;

    private TableView<Pedido>       tablaPedidos;
    private TableView<DetallePedido> tablaDetalle;
    private ComboBox<Cliente>       cbCliente;
    private ComboBox<Producto>      cbProducto;
    private TextField               txtCantidad;
    private Label                   lblMensaje;
    private final ObservableList<DetallePedido> renglones = FXCollections.observableArrayList();

    public VistaPedidos(Stage stage, InterfazPrincipal hub) throws SQLException {
        this.stage      = stage;
        this.hub        = hub;
        this.gestor     = new GestorPedido();
        this.gestorCli  = new GestorCliente();
        this.gestorProd = new GestorProducto();
        this.root       = construir();
        cargarCombos();
        cargarPedidos();
    }

    public Parent getRoot() { return root; }

    private VBox construir() {
        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(25));

        HBox encabezado = encabezado("PEDIDOS");

        // Tabla de pedidos existentes
        tablaPedidos = new TableView<>();
        agregarColPedido("ID",      p -> String.valueOf(p.getIdPedido()));
        agregarColPedido("Cliente", p -> String.valueOf(p.getFkCliente()));
        agregarColPedido("Fecha",   p -> p.getFechaPedido());
        agregarColPedido("Estatus", p -> p.getEstatus());
        tablaPedidos.setPrefHeight(200);

        // Al seleccionar un pedido, carga su detalle
        tablaPedidos.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                try { cargarDetallePedido(sel.getIdPedido()); }
                catch (SQLException ignored) {}
            }
        });

        // Tabla de detalle del pedido seleccionado
        tablaDetalle = new TableView<>();
        agregarColDetalle("Producto ID", d -> String.valueOf(d.getFkProducto()));
        agregarColDetalle("Cantidad",    d -> String.valueOf(d.getCantidad()));
        agregarColDetalle("Precio Unit.", d -> String.format("$%.2f", d.getPrecioUnitario()));
        agregarColDetalle("Subtotal",    d -> String.format("$%.2f", d.getSubtotal()));
        tablaDetalle.setPrefHeight(130);

        // Panel para crear un nuevo pedido
        cbCliente = new ComboBox<>(); cbCliente.setId("cb-cliente-ped"); cbCliente.setPromptText("Cliente...");
        cbProducto = new ComboBox<>(); cbProducto.setId("cb-prod-ped"); cbProducto.setPromptText("Producto...");
        txtCantidad = new TextField(); txtCantidad.setId("txt-cant-ped"); txtCantidad.setPromptText("Cantidad");

        lblMensaje = new Label(); lblMensaje.getStyleClass().add("error");

        // Lista temporal de renglones antes de confirmar el pedido
        TableView<DetallePedido> tablaTemp = new TableView<>(renglones);
        TableColumn<DetallePedido, String> colT1 = new TableColumn<>("Prod.");
        colT1.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getFkProducto())));
        TableColumn<DetallePedido, String> colT2 = new TableColumn<>("Cant.");
        colT2.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getCantidad())));
        tablaTemp.getColumns().addAll(colT1, colT2);
        tablaTemp.setPrefHeight(100);

        Button btnAgregarRenglon = new Button("+ Producto");
        btnAgregarRenglon.setId("btn-add-renglon");
        btnAgregarRenglon.setOnAction(e -> accionAgregarRenglon());

        Button btnConfirmar = new Button("Confirmar Pedido");
        btnConfirmar.setId("btn-confirmar-ped");
        btnConfirmar.setOnAction(e -> accionConfirmarPedido());

        Button btnEntregar = new Button("Marcar Entregado");
        btnEntregar.setId("btn-entregar-ped");
        btnEntregar.setOnAction(e -> accionMarcarEntregado());

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("button-peligro");
        btnEliminar.setId("btn-eliminar-ped");
        btnEliminar.setOnAction(e -> accionEliminar());

        HBox filaCombos = new HBox(10, cbCliente, cbProducto, txtCantidad, btnAgregarRenglon);
        filaCombos.setAlignment(Pos.CENTER_LEFT);
        HBox filaAcciones = new HBox(10, btnConfirmar, btnEntregar, btnEliminar);

        VBox panelNuevo = new VBox(8,
                new Label("Nuevo pedido:"),
                filaCombos,
                new Label("Renglones:"), tablaTemp,
                filaAcciones, lblMensaje);
        panelNuevo.getStyleClass().add("panel-formulario");

        contenedor.getChildren().addAll(encabezado,
                new Label("Pedidos registrados:"), tablaPedidos,
                new Label("Detalle del pedido seleccionado:"), tablaDetalle,
                panelNuevo);
        return contenedor;
    }

    private void accionAgregarRenglon() {
        Producto p = cbProducto.getSelectionModel().getSelectedItem();
        if (p == null) { mostrar("Selecciona un producto.", true); return; }
        try {
            int cant = Integer.parseInt(txtCantidad.getText().trim());
            if (cant <= 0) throw new NumberFormatException();
            // precio_unitario se asigna en el gestor; aquí solo se agrega como 0 temporal
            renglones.add(new DetallePedido(0, p.getIdProducto(), cant, p.getPrecioActual()));
            cbProducto.getSelectionModel().clearSelection();
            txtCantidad.clear();
            mostrar("", false);
        } catch (NumberFormatException e) { mostrar("Cantidad inválida.", true); }
    }

    private void accionConfirmarPedido() {
        Cliente cli = cbCliente.getSelectionModel().getSelectedItem();
        if (cli == null) { mostrar("Selecciona un cliente.", true); return; }
        if (renglones.isEmpty()) { mostrar("Agrega al menos un producto.", true); return; }
        try {
            gestor.crear(cli.getIdCliente(), new ArrayList<>(renglones));
            mostrar("Pedido creado correctamente.", false);
            renglones.clear();
            cbCliente.getSelectionModel().clearSelection();
            cargarPedidos();
        } catch (Exception ex) { mostrar(ex.getMessage(), true); }
    }

    private void accionMarcarEntregado() {
        Pedido sel = tablaPedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrar("Selecciona un pedido.", true); return; }
        try {
            gestor.marcarEntregado(sel.getIdPedido());
            mostrar("Pedido marcado como entregado.", false);
            cargarPedidos();
        } catch (Exception ex) { mostrar(ex.getMessage(), true); }
    }

    private void accionEliminar() {
        Pedido sel = tablaPedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrar("Selecciona un pedido.", true); return; }
        try {
            gestor.eliminar(sel.getIdPedido());
            mostrar("Pedido eliminado.", false);
            tablaDetalle.getItems().clear();
            cargarPedidos();
        } catch (Exception ex) { mostrar(ex.getMessage(), true); }
    }

    private void cargarCombos() throws SQLException {
        cbCliente.setItems(FXCollections.observableArrayList(gestorCli.listarTodos()));
        cbProducto.setItems(FXCollections.observableArrayList(gestorProd.listarTodos()));
    }

    private void cargarPedidos() throws SQLException {
        tablaPedidos.setItems(FXCollections.observableArrayList(gestor.listarTodos()));
    }

    private void cargarDetallePedido(int idPedido) throws SQLException {
        List<DetallePedido> detalle = gestor.obtenerDetalle(idPedido);
        tablaDetalle.setItems(FXCollections.observableArrayList(detalle));
    }

    private void mostrar(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill:#FF2600;" : "-fx-text-fill:#007700;");
    }

    @SuppressWarnings("unchecked")
    private void agregarColPedido(String titulo, java.util.function.Function<Pedido, String> fn) {
        TableColumn<Pedido, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(fn.apply(c.getValue())));
        tablaPedidos.getColumns().add(col);
    }

    @SuppressWarnings("unchecked")
    private void agregarColDetalle(String titulo, java.util.function.Function<DetallePedido, String> fn) {
        TableColumn<DetallePedido, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(fn.apply(c.getValue())));
        tablaDetalle.getColumns().add(col);
    }

    private HBox encabezado(String txt) {
        Label lbl = new Label(txt); lbl.getStyleClass().add("subtitulo");
        Button btn = new Button("← Menú");
        btn.setId("btn-volver-ped");
        btn.setOnAction(e -> stage.getScene().setRoot(hub.getRoot()));
        HBox hb = new HBox(20, btn, lbl); hb.setAlignment(Pos.CENTER_LEFT);
        return hb;
    }
}
