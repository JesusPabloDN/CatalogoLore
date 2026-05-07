package com.catalogos;

/**
 * Representa la relación N:M entre Producto e Insumo.
 * Indica cuánto de un insumo se necesita para fabricar UNA unidad del producto.
 */
public class ProductoInsumo {

    private int    fkProducto;
    private int    fkInsumo;
    private double cantidadNecesaria;

    public ProductoInsumo(int fkProducto, int fkInsumo, double cantidadNecesaria) {
        this.fkProducto        = fkProducto;
        this.fkInsumo          = fkInsumo;
        this.cantidadNecesaria = cantidadNecesaria;
    }

    public int    getFkProducto()         { return fkProducto; }
    public int    getFkInsumo()           { return fkInsumo; }
    public double getCantidadNecesaria()  { return cantidadNecesaria; }

    public void setFkProducto(int fkProducto)              { this.fkProducto        = fkProducto; }
    public void setFkInsumo(int fkInsumo)                  { this.fkInsumo          = fkInsumo; }
    public void setCantidadNecesaria(double cantidad)      { this.cantidadNecesaria = cantidad; }
}
