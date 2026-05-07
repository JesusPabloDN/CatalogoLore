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

/**
 * Genera el catálogo PDF con los productos disponibles.
 * Usa los colores de la paleta del negocio y organiza los productos por categoría.
 */
public class GeneradorPDF {

    // Paleta de colores del negocio
    private static final DeviceRgb COLOR_FONDO    = new DeviceRgb(0xFE, 0xD1, 0x6D);
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

    /**
     * Genera el PDF del catálogo en la ruta indicada.
     * Solo incluye productos con disponibilidad = true.
     *
     * @param rutaDestino Ruta absoluta del archivo PDF a crear.
     * @throws IOException  Si no se puede escribir el archivo.
     * @throws SQLException Si falla la consulta a la BD.
     */
    public void generar(String rutaDestino) throws IOException, SQLException {

        DatosCatalogo datos     = datosCatalogoDAO.obtener();
        List<Producto> productos = productoDAO.obtenerDisponibles();
        List<Categoria> cats    = categoriaDAO.obtenerTodas();

        try (PdfWriter writer   = new PdfWriter(rutaDestino);
             PdfDocument pdf    = new PdfDocument(writer);
             Document documento = new Document(pdf)) {

            // Portada: nombre y descripción del negocio
            agregarPortada(documento, datos);

            // Sección por cada categoría con sus productos
            for (Categoria cat : cats) {
                List<Producto> porCategoria = productos.stream()
                    .filter(p -> p.getFkCategoria() == cat.getIdCategoria())
                    .toList();
                if (!porCategoria.isEmpty()) {
                    agregarSeccionCategoria(documento, cat, porCategoria);
                }
            }

            // Pie de contacto al final del documento
            agregarPieContacto(documento, datos);
        }
    }

    // -------------------------------------------------------------------------

    /** Agrega la portada con el nombre del negocio y su descripción. */
    private void agregarPortada(Document doc, DatosCatalogo datos) {
        // Título principal centrado
        Paragraph titulo = new Paragraph(datos.getNombreNegocio())
                .setFontSize(28)
                .setBold()
                .setFontColor(COLOR_BORDE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        doc.add(titulo);

        // Descripción del negocio (opcional)
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

    /** Agrega el encabezado de categoría y la cuadrícula de productos. */
    private void agregarSeccionCategoria(Document doc, Categoria cat,
                                         List<Producto> productos) {
        // Encabezado de la categoría
        Paragraph encabezado = new Paragraph(cat.getNombre().toUpperCase())
                .setFontSize(16)
                .setBold()
                .setFontColor(COLOR_BORDE)
                .setMarginTop(20)
                .setMarginBottom(10);
        doc.add(encabezado);

        // Tabla de 2 columnas para los productos
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        for (Producto p : productos) {
            tabla.addCell(crearCeldaProducto(p));
        }
        // Si el número de productos es impar, agrega una celda vacía para cuadrar la tabla
        if (productos.size() % 2 != 0) {
            tabla.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        }

        doc.add(tabla);
    }

    /** Crea la celda visual de un producto con imagen, nombre, precio y beneficios. */
    private Cell crearCeldaProducto(Producto p) {
        Cell celda = new Cell()
                .setBackgroundColor(new DeviceRgb(0xFF, 0xEA, 0xAD))
                .setBorder(new SolidBorder(COLOR_BORDE, 1.5f))  // iText 7: borde con SolidBorder
                .setPadding(10);

        // Imagen del producto (si la ruta existe)
        if (p.getRutaImagen() != null && new File(p.getRutaImagen()).exists()) {
            try {
                Image img = new Image(ImageDataFactory.create(p.getRutaImagen()))
                        .setMaxWidth(180)
                        .setAutoScale(true)
                        .setMarginBottom(6);
                celda.add(img);
            } catch (Exception ignored) {
                // Si la imagen falla, continúa sin ella
            }
        }

        // Nombre del producto
        celda.add(new Paragraph(p.getNombre())
                .setBold()
                .setFontSize(13)
                .setFontColor(COLOR_BORDE)
                .setMarginBottom(4));

        // Precio
        celda.add(new Paragraph(String.format("$%.2f", p.getPrecioActual()))
                .setFontSize(12)
                .setFontColor(COLOR_TEXTO)
                .setMarginBottom(4));

        // Beneficios (opcional)
        if (p.getBeneficios() != null && !p.getBeneficios().isBlank()) {
            celda.add(new Paragraph(p.getBeneficios())
                    .setFontSize(10)
                    .setFontColor(COLOR_TEXTO)
                    .setItalic());
        }

        return celda;
    }

    /** Agrega el teléfono de contacto al final del documento. */
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
