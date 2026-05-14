package com.catalogos;

// Define el molde para crear productos del catalogo
public class Producto {

    private int     idProducto;
    private int     fkCategoria;
    private String  nombre;
    private String  beneficios;
    private double  precioActual;
    private String  rutaImagen;
    private boolean disponibilidad; // Guarda si el producto se muestra en el PDF
    private int     stockTerminado;

    public Producto(int idProducto, int fkCategoria, String nombre,
                    String beneficios, double precioActual, String rutaImagen,
                    boolean disponibilidad, int stockTerminado) {
        this.idProducto     = idProducto;
        this.fkCategoria    = fkCategoria;
        this.nombre         = nombre;
        this.beneficios     = beneficios;
        this.precioActual   = precioActual;
        this.rutaImagen     = rutaImagen;
        this.disponibilidad = disponibilidad;
        this.stockTerminado = stockTerminado;
    }

    public int     getIdProducto()     { return idProducto; }
    public int     getFkCategoria()    { return fkCategoria; }
    public String  getNombre()         { return nombre; }
    public String  getBeneficios()     { return beneficios; }
    public double  getPrecioActual()   { return precioActual; }
    public String  getRutaImagen()     { return rutaImagen; }
    public boolean isDisponibilidad()  { return disponibilidad; }
    public int     getStockTerminado() { return stockTerminado; }

    public void setIdProducto(int idProducto)         { this.idProducto     = idProducto; }
    public void setFkCategoria(int fkCategoria)       { this.fkCategoria    = fkCategoria; }
    public void setNombre(String nombre)              { this.nombre         = nombre; }
    public void setBeneficios(String beneficios)      { this.beneficios     = beneficios; }
    public void setPrecioActual(double precioActual)  { this.precioActual   = precioActual; }
    public void setRutaImagen(String rutaImagen)      { this.rutaImagen     = rutaImagen; }
    public void setDisponibilidad(boolean disp)       { this.disponibilidad = disp; }
    public void setStockTerminado(int stock)          { this.stockTerminado = stock; }

    @Override
    public String toString() { return nombre; }
}
