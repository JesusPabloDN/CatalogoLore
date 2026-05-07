package com.catalogos;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Módulo para editar los datos generales del negocio
 * que aparecerán en la portada del catálogo PDF.
 */
public class VistaDatosCatalogo {

    private final Stage              stage;
    private final InterfazPrincipal  hub;
    private final DatosCatalogoDAO   dao;
    private final VBox               root;

    private TextField txtNombre, txtTelefono;
    private TextArea  txtDescripcion;
    private Label     lblMensaje;

    public VistaDatosCatalogo(Stage stage, InterfazPrincipal hub) throws SQLException {
        this.stage = stage;
        this.hub   = hub;
        this.dao   = new DatosCatalogoDAO();
        this.root  = construir();
        cargarDatos();
    }

    public Parent getRoot() { return root; }

    private VBox construir() {
        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(25));

        HBox encabezado = encabezado("DATOS DEL CATÁLOGO");

        txtNombre     = new TextField(); txtNombre.setId("txt-nombre-negocio");
        txtTelefono   = new TextField(); txtTelefono.setId("txt-telefono-negocio");
        txtDescripcion = new TextArea(); txtDescripcion.setId("txt-descripcion-negocio");
        txtDescripcion.setPrefRowCount(4);

        lblMensaje = new Label(); lblMensaje.getStyleClass().add("error");

        Button btnGuardar = new Button("Guardar cambios");
        btnGuardar.setId("btn-guardar-datos");
        btnGuardar.setOnAction(e -> accionGuardar());

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10);
        form.getStyleClass().add("panel-formulario");
        form.addRow(0, new Label("Nombre del negocio:"), txtNombre);
        form.addRow(1, new Label("Teléfono:"),           txtTelefono);
        form.addRow(2, new Label("Descripción:"),        txtDescripcion);
        form.add(btnGuardar, 0, 3, 2, 1);
        form.add(lblMensaje, 0, 4, 2, 1);

        contenedor.getChildren().addAll(encabezado, form);
        return contenedor;
    }

    private void cargarDatos() throws SQLException {
        DatosCatalogo datos = dao.obtener();
        if (datos != null) {
            txtNombre.setText(datos.getNombreNegocio());
            txtTelefono.setText(datos.getTelefonoContacto());
            txtDescripcion.setText(datos.getDescripcion());
        }
    }

    private void accionGuardar() {
        if (txtNombre.getText().isBlank()) {
            mostrar("El nombre del negocio no puede estar vacío.", true); return;
        }
        try {
            DatosCatalogo datos = new DatosCatalogo(1,
                    txtNombre.getText().trim(),
                    txtDescripcion.getText().trim(),
                    txtTelefono.getText().trim());
            dao.actualizar(datos);
            mostrar("Datos guardados correctamente.", false);
        } catch (SQLException ex) {
            mostrar("Error al guardar: " + ex.getMessage(), true);
        }
    }

    private void mostrar(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill:#FF2600;" : "-fx-text-fill:#007700;");
    }

    private HBox encabezado(String txt) {
        Label lbl = new Label(txt); lbl.getStyleClass().add("subtitulo");
        Button btn = new Button("← Menú");
        btn.setId("btn-volver-datos");
        btn.setOnAction(e -> stage.getScene().setRoot(hub.getRoot()));
        HBox hb = new HBox(20, btn, lbl); hb.setAlignment(Pos.CENTER_LEFT);
        return hb;
    }
}
