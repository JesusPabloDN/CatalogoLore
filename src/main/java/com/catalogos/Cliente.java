package com.catalogos;

/** Representa un cliente frecuente del negocio. */
public class Cliente {

    private int    idCliente;
    private String nombreCompleto;
    private String telefono;           // debe tener exactamente 10 dígitos (validar en gestor)
    private String direccionEntrega;

    public Cliente(int idCliente, String nombreCompleto,
                   String telefono, String direccionEntrega) {
        this.idCliente        = idCliente;
        this.nombreCompleto   = nombreCompleto;
        this.telefono         = telefono;
        this.direccionEntrega = direccionEntrega;
    }

    public int    getIdCliente()        { return idCliente; }
    public String getNombreCompleto()   { return nombreCompleto; }
    public String getTelefono()         { return telefono; }
    public String getDireccionEntrega() { return direccionEntrega; }

    public void setIdCliente(int idCliente)              { this.idCliente        = idCliente; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto   = nombreCompleto; }
    public void setTelefono(String telefono)             { this.telefono         = telefono; }
    public void setDireccionEntrega(String direccion)    { this.direccionEntrega = direccion; }

    @Override
    public String toString() { return nombreCompleto; }
}
