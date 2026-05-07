package com.catalogos;

/** Representa el único registro de configuración del negocio (id_datos = 1). */
public class DatosCatalogo {

    private int    idDatos;
    private String nombreNegocio;
    private String descripcion;
    private String telefonoContacto;

    public DatosCatalogo(int idDatos, String nombreNegocio,
                         String descripcion, String telefonoContacto) {
        this.idDatos          = idDatos;
        this.nombreNegocio    = nombreNegocio;
        this.descripcion      = descripcion;
        this.telefonoContacto = telefonoContacto;
    }

    public int    getIdDatos()           { return idDatos; }
    public String getNombreNegocio()     { return nombreNegocio; }
    public String getDescripcion()       { return descripcion; }
    public String getTelefonoContacto()  { return telefonoContacto; }

    public void setIdDatos(int idDatos)                    { this.idDatos          = idDatos; }
    public void setNombreNegocio(String nombreNegocio)     { this.nombreNegocio    = nombreNegocio; }
    public void setDescripcion(String descripcion)         { this.descripcion      = descripcion; }
    public void setTelefonoContacto(String tel)            { this.telefonoContacto = tel; }
}
