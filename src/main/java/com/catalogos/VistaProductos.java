package com.catalogos;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Módulo de gestión de productos.
 * Permite listar, agregar, editar y eliminar productos,
 * con selección de imagen y asignación de categoría.
 */
public class VistaProductos {

    private final Stage             stage;
    private final InterfazPrincipal hub;
    private final GestorProducto    gestor;
    private final GestorCategoria   gestorCat;
    private final VBox              root;

    private TableView<Producto> tabla;
    private TextField  txtNombre, txtPrecio, txtBeneficios, txtImagen;
    private ComboBox<Categoria> cbCategoria;
    private CheckBox   chkDisponible;
    private Label      lblMensaje;

    public VistaProductos(Stage stage, InterfazPrincipal hub) throws SQLException {
        this.stage      = stage;
        this.hub        = hub;
        this.gestor     = new GestorProducto();
        this.gestorCat  = new GestorCategoria();
        this.root       = construir();
        cargarTabla();
    }

    public Parent getRoot() { return root; }

    private VBox construir() {
        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(25));

        HBox encabezado = encabezado("PRODUCTOS");

        // Tabla
        tabla = new TableView<>();
        agregarColumna("Nombre",      p -> p.getNombre());
        agregarColumna("Precio",      p -> String.format("$%.2f", p.getPrecioActual()));
        agregarColumna("Disponible",  p -> p.isDisponibilidad() ? "Sí" : "No");
        agregarColumna("Stock",       p -> String.valueOf(p.getStockTerminado()));
        tabla.setPrefHeight(250);

        // Rellena formulario al seleccionar fila
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) rellenarFormulario(sel);
        });

        // Formulario
        txtNombre     = campo("txt-nombre-prod",   "Nombre del producto");
        txtPrecio     = campo("txt-precio-prod",   "Precio (ej. 45.50)");
        txtBeneficios = campo("txt-beneficios",    "Beneficios / descripción");
        txtImagen     = campo("txt-imagen",        "Ruta de la imagen");
        txtImagen.setEditable(false);

        Button btnImagen = new Button("Buscar imagen...");
        btnImagen.setId("btn-buscar-imagen");
        btnImagen.setOnAction(e -> seleccionarImagen());

        cbCategoria = new ComboBox<>();
        cbCategoria.setId("cb-categoria-prod");
        cbCategoria.setPromptText("Categoría");
        try { cbCategoria.setItems(FXCollections.observableArrayList(gestorCat.listar())); }
        catch (SQLException ignored) {}

        chkDisponible = new CheckBox("Disponible en catálogo");
        chkDisponible.setSelected(true);

        lblMensaje = new Label();
        lblMensaje.getStyleClass().add("error");

        Button btnAgregar  = new Button("Agregar");
        Button btnEditar   = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("button-peligro");
        btnAgregar.setId("btn-agregar-prod");
        btnEditar.setId("btn-editar-prod");
        btnEliminar.setId("btn-eliminar-prod");

        btnAgregar.setOnAction(e  -> accionAgregar());
        btnEditar.setOnAction(e   -> accionEditar());
        btnEliminar.setOnAction(e -> accionEliminar());

        HBox imagenRow  = new HBox(8, txtImagen, btnImagen);
        HBox botonesRow = new HBox(10, btnAgregar, btnEditar, btnEliminar);

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8);
        form.getStyleClass().add("panel-formulario");
        form.addRow(0, new Label("Nombre:"),     txtNombre);
        form.addRow(1, new Label("Precio:"),     txtPrecio);
        form.addRow(2, new Label("Beneficios:"), txtBeneficios);
        form.addRow(3, new Label("Imagen:"),     imagenRow);
        form.addRow(4, new Label("Categoría:"),  cbCategoria);
        form.addRow(5, chkDisponible, new Label(""));
        form.add(botonesRow, 0, 6, 2, 1);
        form.add(lblMensaje, 0, 7, 2, 1);

        contenedor.getChildren().addAll(encabezado, tabla, form);
        return contenedor;
    }

    // Acciones CRUD
    private void accionAgregar() {
        try {
            Producto p = leerFormulario(0);
            gestor.agregar(p);
            mostrar("Producto agregado.", false);
            cargarTabla(); limpiar();
        } catch (Exception ex) { mostrar(ex.getMessage(), true); }
    }

    private void accionEditar() {
        Producto sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrar("Selecciona un producto.", true); return; }
        try {
            Producto p = leerFormulario(sel.getIdProducto());
            gestor.actualizar(p);
            mostrar("Producto actualizado.", false);
            cargarTabla();
        } catch (Exception ex) { mostrar(ex.getMessage(), true); }
    }

    private void accionEliminar() {
        Producto sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrar("Selecciona un producto.", true); return; }
        try {
            gestor.eliminar(sel.getIdProducto());
            mostrar("Producto eliminado.", false);
            cargarTabla(); limpiar();
        } catch (Exception ex) { mostrar("No se puede eliminar: tiene pedidos asociados.", true); }
    }

    private void seleccionarImagen() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar imagen");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File archivo = fc.showOpenDialog(stage);
        if (archivo != null) txtImagen.setText(archivo.getAbsolutePath());
    }

    // Helpers
    private Producto leerFormulario(int id) {
        Categoria cat = cbCategoria.getSelectionModel().getSelectedItem();
        int idCat = (cat != null) ? cat.getIdCategoria() : 0;
        double precio = 0;
        try { precio = Double.parseDouble(txtPrecio.getText().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Precio inválido."); }
        return new Producto(id, idCat, txtNombre.getText().trim(),
                txtBeneficios.getText().trim(), precio,
                txtImagen.getText().trim(), chkDisponible.isSelected(), 0);
    }

    private void rellenarFormulario(Producto p) {
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(String.valueOf(p.getPrecioActual()));
        txtBeneficios.setText(p.getBeneficios() != null ? p.getBeneficios() : "");
        txtImagen.setText(p.getRutaImagen() != null ? p.getRutaImagen() : "");
        chkDisponible.setSelected(p.isDisponibilidad());
        try {
            List<Categoria> cats = gestorCat.listar();
            cats.stream().filter(c -> c.getIdCategoria() == p.getFkCategoria())
                .findFirst().ifPresent(c -> cbCategoria.getSelectionModel().select(c));
        } catch (SQLException ignored) {}
    }

    private void limpiar() {
        txtNombre.clear(); txtPrecio.clear();
        txtBeneficios.clear(); txtImagen.clear();
        cbCategoria.getSelectionModel().clearSelection();
        chkDisponible.setSelected(true);
    }

    private void cargarTabla() throws SQLException {
        tabla.setItems(FXCollections.observableArrayList(gestor.listarTodos()));
    }

    private void mostrar(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill:#FF2600;" : "-fx-text-fill:#007700;");
    }

    @SuppressWarnings("unchecked")
    private void agregarColumna(String titulo, java.util.function.Function<Producto, String> fn) {
        TableColumn<Producto, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(fn.apply(c.getValue())));
        tabla.getColumns().add(col);
    }

    private TextField campo(String id, String prompt) {
        TextField tf = new TextField();
        tf.setId(id); tf.setPromptText(prompt);
        return tf;
    }

    private HBox encabezado(String txt) {
        Label lbl = new Label(txt); lbl.getStyleClass().add("subtitulo");
        Button btn = new Button("← Menú");
        btn.setId("btn-volver-prod");
        btn.setOnAction(e -> stage.getScene().setRoot(hub.getRoot()));
        HBox hb = new HBox(20, btn, lbl); hb.setAlignment(Pos.CENTER_LEFT);
        return hb;
    }
}
