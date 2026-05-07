package com.catalogos;

import java.sql.SQLException;
import java.util.List;

/**
 * Gestiona la lógica de negocio para insumos (materia prima).
 * Valida datos antes de delegar al DAO.
 */
public class GestorInsumo {

    private final InsumoDAO dao;

    public GestorInsumo() throws SQLException {
        this.dao = new InsumoDAO();
    }

    /**
     * Registra un insumo nuevo tras validar:
     * - Nombre no vacío.
     * - Stock inicial >= 0.
     * - Unidad de medida no vacía.
     */
    public void agregar(String nombre, double stockInicial, String unidadMedida) throws SQLException {
        validar(nombre, stockInicial, unidadMedida);
        Insumo insumo = new Insumo(0, nombre.trim(), stockInicial, unidadMedida.trim());
        dao.insertar(insumo);
    }

    /** Actualiza los datos de un insumo existente (mismas validaciones). */
    public void actualizar(Insumo insumo) throws SQLException {
        validar(insumo.getNombre(), insumo.getStockActual(), insumo.getUnidadMedida());
        dao.actualizar(insumo);
    }

    /**
     * Incrementa el stock de un insumo en la cantidad indicada.
     * La cantidad a agregar debe ser > 0.
     */
    public void agregarStock(int idInsumo, double cantidad) throws SQLException {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        Insumo insumo = dao.obtenerPorId(idInsumo);
        if (insumo == null) throw new IllegalArgumentException("Insumo no encontrado: " + idInsumo);
        insumo.setStockActual(insumo.getStockActual() + cantidad);
        dao.actualizar(insumo);
    }

    /** Elimina un insumo. Sus recetas asociadas se borran por CASCADE en la BD. */
    public void eliminar(int idInsumo) throws SQLException {
        dao.eliminar(idInsumo);
    }

    public List<Insumo> listarTodos()      throws SQLException { return dao.obtenerTodos(); }
    public Insumo       obtenerPorId(int id) throws SQLException { return dao.obtenerPorId(id); }

    // Validaciones básicas de negocio
    private void validar(String nombre, double stock, String unidad) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del insumo no puede estar vacío.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo.");
        }
        if (unidad == null || unidad.isBlank()) {
            throw new IllegalArgumentException("La unidad de medida no puede estar vacía.");
        }
    }
}
