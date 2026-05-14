package com.catalogos;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;

// Pantalla para exportar el catalogo a PDF
public class VistaGenerarPDF {

    private final Stage             stage;
    private final InterfazPrincipal hub;
    private final VBox              root;

    private TextField txtRuta;
    private Label     lblMensaje;

    public VistaGenerarPDF(Stage stage, InterfazPrincipal hub) {
        this.stage = stage;
        this.hub   = hub;
        this.root  = construir();
    }

    public Parent getRoot() { return root; }

    private VBox construir() {
        VBox contenedor = new VBox(20);
        contenedor.setPadding(new Insets(30));
        contenedor.setAlignment(Pos.TOP_CENTER);

        HBox encabezado = encabezado("GENERAR PDF");

        Label instruccion = new Label(
                "Se exportarán todos los productos marcados como disponibles.\n"
              + "Elige la carpeta donde se guardará el archivo.");
        instruccion.setWrapText(true);

        txtRuta = new TextField();
        txtRuta.setId("txt-ruta-pdf");
        txtRuta.setPromptText("Carpeta de destino...");
        txtRuta.setEditable(false);
        txtRuta.setMaxWidth(500);

        Button btnCarpeta = new Button("Seleccionar carpeta...");
        btnCarpeta.setId("btn-carpeta-pdf");
        btnCarpeta.setOnAction(e -> seleccionarCarpeta());

        lblMensaje = new Label();
        lblMensaje.getStyleClass().add("error");
        lblMensaje.setWrapText(true);

        Button btnGenerar = new Button("✦ Generar Catálogo PDF");
        btnGenerar.setId("btn-generar-pdf");
        btnGenerar.getStyleClass().add("boton-menu");
        btnGenerar.setPrefWidth(280);
        btnGenerar.setOnAction(e -> accionGenerar());

        HBox filaRuta = new HBox(10, txtRuta, btnCarpeta);
        filaRuta.setAlignment(Pos.CENTER);

        contenedor.getChildren().addAll(encabezado, instruccion, filaRuta, btnGenerar, lblMensaje);
        return contenedor;
    }

    private void seleccionarCarpeta() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Elegir carpeta de destino");
        File carpeta = dc.showDialog(stage);
        if (carpeta != null) txtRuta.setText(carpeta.getAbsolutePath());
    }

    private void accionGenerar() {
        String carpeta = txtRuta.getText().trim();
        if (carpeta.isEmpty()) {
            mostrar("Selecciona la carpeta de destino primero.", true); return;
        }
        // Crea el nombre del archivo usando la fecha de hoy
        String nombreArchivo = "catalogo_" + LocalDate.now() + ".pdf";
        String rutaCompleta  = carpeta + File.separator + nombreArchivo;

        try {
            GeneradorPDF generador = new GeneradorPDF();
            generador.generar(rutaCompleta);
            mostrar("PDF generado en:\n" + rutaCompleta, false);
        } catch (Exception ex) {
            mostrar("Error al generar el PDF: " + ex.getMessage(), true);
        }
    }

    private void mostrar(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill:#FF2600;" : "-fx-text-fill:#007700;");
    }

    private HBox encabezado(String txt) {
        Label lbl = new Label(txt); lbl.getStyleClass().add("subtitulo");
        Button btn = new Button("← Menú");
        btn.setId("btn-volver-pdf");
        btn.setOnAction(e -> stage.getScene().setRoot(hub.getRoot()));
        HBox hb = new HBox(20, btn, lbl); hb.setAlignment(Pos.CENTER_LEFT);
        return hb;
    }
}
