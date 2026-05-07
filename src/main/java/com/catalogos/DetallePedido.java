package com.catalogos;

/**
 * Representa un renglón del pedido: un producto y su cantidad.
 * precioUnitario captura el precio al momento del pedido (precio histórico);
 * no cambia si el producto se actualiza después.
 */
public class DetallePedido {

    private int    fkPedido;
    private int    fkProducto;
    private int    cantidad;
    private double precioUnitario;

    public DetallePedido(int fkPedido, int fkProducto, int cantidad, double precioUnitario) {
        this.fkPedido       = fkPedido;
        this.fkProducto     = fkProducto;
        this.cantidad       = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public int    getFkPedido()       { return fkPedido; }
    public int    getFkProducto()     { return fkProducto; }
    public int    getCantidad()       { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }

    public void setFkPedido(int fkPedido)            { this.fkPedido       = fkPedido; }
    public void setFkProducto(int fkProducto)        { this.fkProducto     = fkProducto; }
    public void setCantidad(int cantidad)            { this.cantidad       = cantidad; }
    public void setPrecioUnitario(double precio)     { this.precioUnitario = precio; }

    /** Subtotal del renglón. */
    public double getSubtotal() { return cantidad * precioUnitario; }
}
