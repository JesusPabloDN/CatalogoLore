package com.catalogos;

// Define la estructura para la materia prima del inventario
public class Insumo {

    private int    idInsumo;
    private String nombre;
    private double stockActual;
    private String unidadMedida; // Guarda la unidad de medida


    public Insumo(int idInsumo, String nombre, double stockActual, String unidadMedida) {
        this.idInsumo     = idInsumo;
        this.nombre       = nombre;
        this.stockActual  = stockActual;
        this.unidadMedida = unidadMedida;
    }

    public int    getIdInsumo()     { return idInsumo; }
    public String getNombre()       { return nombre; }
    public double getStockActual()  { return stockActual; }
    public String getUnidadMedida() { return unidadMedida; }

    public void setIdInsumo(int idInsumo)         { this.idInsumo     = idInsumo; }
    public void setNombre(String nombre)          { this.nombre       = nombre; }
    public void setStockActual(double stock)      { this.stockActual  = stock; }
    public void setUnidadMedida(String unidad)    { this.unidadMedida = unidad; }

    @Override
    public String toString() { return nombre + " (" + unidadMedida + ")"; }
}
