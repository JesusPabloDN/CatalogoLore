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

// Pantalla para registrar y administrar pedidos
public class VistaPedidos {

    private final Stage             stage;
    private final InterfazPrincipal hub;
    private final GestorPedido      gestor;
    private final GestorCliente     gestorCli;
    private final GestorProducto    gestorProd;
    private final VBox              root;

    // Tabla general de los pedidos
    private TableView<String[]> tablaUnificada;

    // Tabla para armar un pedido nuevo antes de guardarlo
    private final ObservableList<String[]> renglonesMostrar = FXCollections.observableArrayList();
    // Guarda la informacion de los productos del pedido
    private final List<DetallePedido>      renglonesReales  = new ArrayList<>();

    private ComboBox<Cliente>  cbCliente;
    private ComboBox<Producto> cbProducto;
    private TextField          txtCantidad;
    private Label              lblMensaje;

    // Id del pedido que se le dio clic
    private int idPedidoSeleccionado = -1;

    public VistaPedidos(Stage stage, InterfazPrincipal hub) throws SQLException {
        this.stage      = stage;
        this.hub        = hub;
        this.gestor     = new GestorPedido();
        this.gestorCli  = new GestorCliente();
        this.gestorProd = new GestorProducto();
        this.root       = construir();
        cargarCombos();
        cargarTablaUnificada();
    }

    public Parent getRoot() { return root; }

    // --- Creacion de la pantalla ---
    private VBox construir() {
        VBox contenedor = new VBox(14);
        contenedor.setPadding(new Insets(22));

        HBox encabezado = encabezado("PEDIDOS");

        // Tabla principal
        tablaUnificada = new TableView<>();
        tablaUnificada.setPrefHeight(260);
        tablaUnificada.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Columnas de la tabla
        tablaUnificada.getColumns().add(colStr("ID",          0,  45));
        tablaUnificada.getColumns().add(colStr("Cliente",     1, 150));
        tablaUnificada.getColumns().add(colStr("Fecha",       2,  90));
        tablaUnificada.getColumns().add(colStr("Estatus",     3,  85));
        tablaUnificada.getColumns().add(colStr("Producto",    4, 150));
        tablaUnificada.getColumns().add(colStr("Cant.",       5,  55));
        tablaUnificada.getColumns().add(colStr("Precio Unit.",6, 110));
        tablaUnificada.getColumns().add(colStr("Subtotal",    7, 100));

        // Guarda el ID del pedido cuando se hace clic en la tabla
        tablaUnificada.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> {
                    if (sel != null) idPedidoSeleccionado = Integer.parseInt(sel[0]);
                });

        // Tabla del nuevo pedido
        TableView<String[]> tablaTemp = new TableView<>(renglonesMostrar);
        tablaTemp.setPrefHeight(100);
        tablaTemp.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        // Columnas
        tablaTemp.getColumns().add(colStr("Producto",     0, 180));
        tablaTemp.getColumns().add(colStr("Cantidad",     1,  80));
        tablaTemp.getColumns().add(colStr("Precio Unit.", 2, 120));

        // Cuadros para llenar el pedido
        cbCliente  = new ComboBox<>(); cbCliente.setId("cb-cliente-ped");
        cbCliente.setPromptText("Cliente..."); cbCliente.setPrefWidth(200);
        cbProducto = new ComboBox<>(); cbProducto.setId("cb-prod-ped");
        cbProducto.setPromptText("Producto..."); cbProducto.setPrefWidth(200);
        txtCantidad = new TextField(); txtCantidad.setId("txt-cant-ped");
        txtCantidad.setPromptText("Cant."); txtCantidad.setPrefWidth(80);

        lblMensaje = new Label();
        lblMensaje.getStyleClass().add("error");
        lblMensaje.setWrapText(true);

        Button btnAddRenglon = new Button("+ Agregar producto");
        Button btnConfirmar  = new Button("Confirmar Pedido");
        Button btnEntregar   = new Button("Marcar Entregado");
        Button btnEliminar   = new Button("Eliminar Pedido");
        btnEliminar.getStyleClass().add("button-peligro");

        btnAddRenglon.setId("btn-add-renglon");
        btnConfirmar.setId("btn-confirmar-ped");
        btnEntregar.setId("btn-entregar-ped");
        btnEliminar.setId("btn-eliminar-ped");

        btnAddRenglon.setOnAction(e -> accionAgregarRenglon());
        btnConfirmar.setOnAction(e  -> accionConfirmarPedido());
        btnEntregar.setOnAction(e   -> accionMarcarEntregado());
        btnEliminar.setOnAction(e   -> accionEliminar());

        HBox filaCombos   = new HBox(10, cbCliente, cbProducto, txtCantidad, btnAddRenglon);
        filaCombos.setAlignment(Pos.CENTER_LEFT);
        HBox filaAcciones = new HBox(10, btnConfirmar, btnEntregar, btnEliminar);
        filaAcciones.setAlignment(Pos.CENTER_LEFT);

        VBox panelNuevo = new VBox(8,
                new Label("Nuevo pedido — selecciona cliente y agrega productos:"),
                filaCombos,
                new Label("Datos del nuevo pedido:"),
                tablaTemp,
                filaAcciones,
                lblMensaje);
        panelNuevo.getStyleClass().add("panel-formulario");

        contenedor.getChildren().addAll(
                encabezado,
                new Label("Pedidos registrados:"),
                tablaUnificada,
                panelNuevo
        );
        return contenedor;
    }

    // --- Funciones de los botones ---

    // Agrega un producto a la lista del pedido nuevo
    private void accionAgregarRenglon() {
        Producto p = cbProducto.getSelectionModel().getSelectedItem();
        if (p == null) { mostrar("Selecciona un producto.", true); return; }
        try {
            int cant = Integer.parseInt(txtCantidad.getText().trim());
            if (cant <= 0) throw new NumberFormatException();

            // Muestra los datos en la tabla
            renglonesMostrar.add(new String[]{
                p.getNombre(),
                String.valueOf(cant),
                String.format("$%.2f", p.getPrecioActual())
            });
            // Guarda los datos ocultos para la base de datos
            renglonesReales.add(new DetallePedido(0, p.getIdProducto(), cant, p.getPrecioActual()));

            cbProducto.getSelectionModel().clearSelection();
            txtCantidad.clear();
            mostrar("", false);
        } catch (NumberFormatException e) {
            mostrar("Ingresa una cantidad válida (número entero > 0).", true);
        }
    }

    private void accionConfirmarPedido() {
        Cliente cli = cbCliente.getSelectionModel().getSelectedItem();
        if (cli == null)          { mostrar("Selecciona un cliente.", true); return; }
        if (renglonesReales.isEmpty()) { mostrar("Agrega al menos un producto.", true); return; }
        try {
            gestor.crear(cli.getIdCliente(), new ArrayList<>(renglonesReales));
            mostrar("Pedido creado correctamente.", false);
            renglonesMostrar.clear();
            renglonesReales.clear();
            cbCliente.getSelectionModel().clearSelection();
            cargarTablaUnificada();
        } catch (Exception ex) {
            mostrar(ex.getMessage(), true);
        }
    }

    private void accionMarcarEntregado() {
        if (idPedidoSeleccionado < 0) { mostrar("Selecciona un pedido en la tabla.", true); return; }
        try {
            gestor.marcarEntregado(idPedidoSeleccionado);
            mostrar("Pedido marcado como entregado.", false);
            cargarTablaUnificada();
        } catch (Exception ex) {
            mostrar(ex.getMessage(), true);
        }
    }

    private void accionEliminar() {
        if (idPedidoSeleccionado < 0) { mostrar("Selecciona un pedido en la tabla.", true); return; }
        try {
            gestor.eliminar(idPedidoSeleccionado);
            mostrar("Pedido eliminado.", false);
            idPedidoSeleccionado = -1;
            cargarTablaUnificada();
        } catch (Exception ex) {
            mostrar(ex.getMessage(), true);
        }
    }

    // --- Funciones para cargar informacion ---

    private void cargarCombos() throws SQLException {
        cbCliente.setItems(FXCollections.observableArrayList(gestorCli.listarTodos()));
        cbProducto.setItems(FXCollections.observableArrayList(gestorProd.listarTodos()));
    }

    private void cargarTablaUnificada() throws SQLException {
        List<String[]> lista = gestor.listarFlattenado();
        tablaUnificada.setItems(FXCollections.observableArrayList(lista));
    }

    // --- Funciones de ayuda ---

    private TableColumn<String[], String> colStr(String titulo, int idx, double ancho) {
        TableColumn<String[], String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue()[idx]));
        col.setPrefWidth(ancho);
        col.setMinWidth(ancho);
        col.setResizable(false);
        return col;
    }

    private void mostrar(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill:#FF2600;" : "-fx-text-fill:#007700;");
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
