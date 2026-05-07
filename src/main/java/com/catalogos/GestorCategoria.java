package com.catalogos;

import java.sql.SQLException;
import java.util.List;

/**
 * Gestiona la lógica de negocio para categorías.
 * Valida reglas antes de delegar al DAO; nunca ejecuta SQL directamente.
 */
public class GestorCategoria {

    private final CategoriaDAO dao;

    public GestorCategoria() throws SQLException {
        this.dao = new CategoriaDAO();
    }

    /**
     * Registra una nueva categoría tras validar que:
     * - El nombre no esté vacío.
     * - No supere 30 caracteres.
     */
    public void agregar(String nombre) throws SQLException {
        validarNombre(nombre);
        Categoria categoria = new Categoria(0, 1, nombre.trim());
        dao.insertar(categoria);
    }

    /**
     * Actualiza el nombre de una categoría existente.
     * Aplica las mismas validaciones que al agregar.
     */
    public void actualizar(int idCategoria, String nuevoNombre) throws SQLException {
        validarNombre(nuevoNombre);
        Categoria categoria = new Categoria(idCategoria, 1, nuevoNombre.trim());
        dao.actualizar(categoria);
    }

    /**
     * Elimina una categoría. Lanzará excepción de BD si tiene productos asociados
     * (la restricción RESTRICT en la llave foránea lo impide).
     */
    public void eliminar(int idCategoria) throws SQLException {
        dao.eliminar(idCategoria);
    }

    /** Devuelve todas las categorías disponibles. */
    public List<Categoria> listar() throws SQLException {
        return dao.obtenerTodas();
    }

    // Valida que el nombre no sea nulo, vacío ni supere 30 caracteres
    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        if (nombre.trim().length() > 30) {
            throw new IllegalArgumentException("El nombre no puede superar 30 caracteres.");
        }
    }
}
