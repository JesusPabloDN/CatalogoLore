package com.catalogos;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// Se encarga de crear el archivo PDF del catalogo
public class GeneradorPDF {

    // Colores que se van a usar en el PDF
    private static final DeviceRgb COLOR_ACENTO   = new DeviceRgb(0xFF, 0xA1, 0x00);
    private static final DeviceRgb COLOR_BORDE    = new DeviceRgb(0x75, 0x44, 0x05);
    private static final DeviceRgb COLOR_TEXTO    = new DeviceRgb(0x00, 0x00, 0x00);

    private final DatosCatalogoDAO datosCatalogoDAO;
    private final ProductoDAO      productoDAO;
    private final CategoriaDAO     categoriaDAO;

    public GeneradorPDF() throws SQLException {
        this.datosCatalogoDAO = new DatosCatalogoDAO();
        this.productoDAO      = new ProductoDAO();
        this.categoriaDAO     = new CategoriaDAO();
    }

    // Crea el PDF en la ruta que se le indique
    public void generar(String rutaDestino) throws IOException, SQLException {

        DatosCatalogo datos     = datosCatalogoDAO.obtener();
        List<Producto> productos = productoDAO.obtenerDisponibles();
        List<Categoria> cats    = categoriaDAO.obtenerTodas();

        try (PdfWriter writer   = new PdfWriter(rutaDestino);
             PdfDocument pdf    = new PdfDocument(writer);
             Document documento = new Document(pdf)) {

            // Coloca el titulo del negocio al principio
            agregarPortada(documento, datos);

            // Agrega los productos separados por su categoria
            for (Categoria cat : cats) {
                List<Producto> porCategoria = productos.stream()
                    .filter(p -> p.getFkCategoria() == cat.getIdCategoria())
                    .toList();
                if (!porCategoria.isEmpty()) {
                    agregarSeccionCategoria(documento, cat, porCategoria);
                }
            }

            // Coloca el telefono al final de la hoja
            agregarPieContacto(documento, datos);
        }
    }

    // --- Metodos internos para armar el PDF ---

    // Dibuja el titulo principal del PDF
    private void agregarPortada(Document doc, DatosCatalogo datos) {
        // Titulo en grande
        Paragraph titulo = new Paragraph(datos.getNombreNegocio())
                .setFontSize(28)
                .setBold()
                .setFontColor(COLOR_BORDE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        doc.add(titulo);

        // Texto con la descripcion debajo del titulo
        if (datos.getDescripcion() != null && !datos.getDescripcion().isBlank()) {
            Paragraph desc = new Paragraph(datos.getDescripcion())
                    .setFontSize(13)
                    .setFontColor(COLOR_TEXTO)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            doc.add(desc);
        }

        doc.add(new LineSeparator(
                new com.itextpdf.kernel.pdf.canvas.draw.SolidLine())
                .setStrokeColor(COLOR_ACENTO)
                .setMarginBottom(20));
    }

    // Dibuja el titulo de la categoria y la lista de sus productos
    private void agregarSeccionCategoria(Document doc, Categoria cat,
                                         List<Producto> productos) {
        // Titulo de la categoria
        Paragraph encabezado = new Paragraph(cat.getNombre().toUpperCase())
                .setFontSize(16)
                .setBold()
                .setFontColor(COLOR_BORDE)
                .setMarginTop(20)
                .setMarginBottom(10);
        doc.add(encabezado);

        // Crea una tabla de dos cuadros
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        for (Producto p : productos) {
            tabla.addCell(crearCeldaProducto(p));
        }
        // Agrega un cuadro vacio si sobra espacio
        if (productos.size() % 2 != 0) {
            tabla.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        }

        doc.add(tabla);
    }

    // Crea el cuadro con la informacion de un producto
    private Cell crearCeldaProducto(Producto p) {
        Cell celda = new Cell()
                .setBackgroundColor(new DeviceRgb(0xFF, 0xEA, 0xAD))
                .setBorder(new SolidBorder(COLOR_BORDE, 1.5f))  // iText 7: borde con SolidBorder
                .setPadding(10);

        // Pone la imagen
        if (p.getRutaImagen() != null && new File(p.getRutaImagen()).exists()) {
            try {
                Image img = new Image(ImageDataFactory.create(p.getRutaImagen()))
                        .setMaxWidth(180)
                        .setAutoScale(true)
                        .setMarginBottom(6);
                celda.add(img);
            } catch (Exception ignored) {
                // Si no se puede poner la imagen se ignora
            }
        }

        // Pone el nombre
        celda.add(new Paragraph(p.getNombre())
                .setBold()
                .setFontSize(13)
                .setFontColor(COLOR_BORDE)
                .setMarginBottom(4));

        // Pone el precio
        celda.add(new Paragraph(String.format("$%.2f", p.getPrecioActual()))
                .setFontSize(12)
                .setFontColor(COLOR_TEXTO)
                .setMarginBottom(4));

        // Pone los beneficios
        if (p.getBeneficios() != null && !p.getBeneficios().isBlank()) {
            celda.add(new Paragraph(p.getBeneficios())
                    .setFontSize(10)
                    .setFontColor(COLOR_TEXTO)
                    .setItalic());
        }

        return celda;
    }

    // Dibuja el numero de telefono al final
    private void agregarPieContacto(Document doc, DatosCatalogo datos) {
        doc.add(new LineSeparator(
                new com.itextpdf.kernel.pdf.canvas.draw.SolidLine())
                .setStrokeColor(COLOR_ACENTO)
                .setMarginTop(30)
                .setMarginBottom(10));

        if (datos.getTelefonoContacto() != null && !datos.getTelefonoContacto().isBlank()) {
            doc.add(new Paragraph("Contacto: " + datos.getTelefonoContacto())
                    .setFontSize(12)
                    .setFontColor(COLOR_TEXTO)
                    .setTextAlignment(TextAlignment.CENTER));
        }
    }
}
