package com.catalogos;

/**
 * Representa una orden de venta.
 * fechaPedido se almacena en formato ISO 8601 (YYYY-MM-DD).
 * estatus solo puede ser "Pendiente" o "Entregado".
 */
public class Pedido {

    private int    idPedido;
    private int    fkCliente;
    private String fechaPedido;
    private String estatus;

    public Pedido(int idPedido, int fkCliente, String fechaPedido, String estatus) {
        this.idPedido    = idPedido;
        this.fkCliente   = fkCliente;
        this.fechaPedido = fechaPedido;
        this.estatus     = estatus;
    }

    public int    getIdPedido()    { return idPedido; }
    public int    getFkCliente()   { return fkCliente; }
    public String getFechaPedido() { return fechaPedido; }
    public String getEstatus()     { return estatus; }

    public void setIdPedido(int idPedido)        { this.idPedido    = idPedido; }
    public void setFkCliente(int fkCliente)      { this.fkCliente   = fkCliente; }
    public void setFechaPedido(String fecha)     { this.fechaPedido = fecha; }
    public void setEstatus(String estatus)       { this.estatus     = estatus; }
}
