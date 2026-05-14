package com.catalogos;

import java.sql.SQLException;
import java.util.List;

// Revisa que los datos de las categorias sean correctos antes de guardarlos
public class GestorCategoria {

    private final CategoriaDAO dao;

    public GestorCategoria() throws SQLException {
        this.dao = new CategoriaDAO();
    }

    // Revisa los datos y guarda una nueva categoria
    public void agregar(String nombre) throws SQLException {
        validarNombre(nombre);
        if (dao.existePorNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
        dao.insertar(new Categoria(0, 1, nombre.trim()));
    }

    // Revisa los datos y modifica el nombre de una categoria
    public void actualizar(int idCategoria, String nuevoNombre) throws SQLException {
        validarNombre(nuevoNombre);
        // Revisa que no exista otra categoria que se llame igual
        Categoria existente = dao.obtenerTodas().stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nuevoNombre.trim())
                          && c.getIdCategoria() != idCategoria)
                .findFirst().orElse(null);
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
        dao.actualizar(new Categoria(idCategoria, 1, nuevoNombre.trim()));
    }

    // Borra una categoria si no tiene productos adentro
    public void eliminar(int idCategoria) throws SQLException {
        try {
            dao.eliminar(idCategoria);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se puede eliminar: la categoría tiene productos asociados.", e);
        }
    }

    // Obtiene la lista de todas las categorias
    public List<Categoria> listar() throws SQLException {
        return dao.obtenerTodas();
    }

    // Revisa que el nombre tenga texto y no sea muy largo
    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        if (nombre.trim().length() > 30) {
            throw new IllegalArgumentException("El nombre no puede superar 30 caracteres.");
        }
    }
}
