package com.catalogos;

/** Representa una categoría que agrupa productos del catálogo. */
public class Categoria {

    private int    idCategoria;
    private int    fkDatos;
    private String nombre;

    public Categoria(int idCategoria, int fkDatos, String nombre) {
        this.idCategoria = idCategoria;
        this.fkDatos     = fkDatos;
        this.nombre      = nombre;
    }

    public int    getIdCategoria() { return idCategoria; }
    public int    getFkDatos()     { return fkDatos; }
    public String getNombre()      { return nombre; }

    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }
    public void setFkDatos(int fkDatos)         { this.fkDatos     = fkDatos; }
    public void setNombre(String nombre)        { this.nombre      = nombre; }

    @Override
    public String toString() { return nombre; }
}
