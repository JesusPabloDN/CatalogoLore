package com.catalogos;

import java.sql.SQLException;
import java.util.List;

// Revisa los datos de la materia prima antes de guardarla
public class GestorInsumo {

    private final InsumoDAO dao;

    public GestorInsumo() throws SQLException {
        this.dao = new InsumoDAO();
    }

    // Revisa los datos y guarda un nuevo insumo
    public void agregar(String nombre, double stockInicial, String unidadMedida) throws SQLException {
        validar(nombre, stockInicial, unidadMedida);
        Insumo insumo = new Insumo(0, nombre.trim(), stockInicial, unidadMedida.trim());
        dao.insertar(insumo);
    }

    // Revisa los datos y modifica un insumo
    public void actualizar(Insumo insumo) throws SQLException {
        validar(insumo.getNombre(), insumo.getStockActual(), insumo.getUnidadMedida());
        dao.actualizar(insumo);
    }

    // Le suma una cantidad al stock actual del insumo
    public void agregarStock(int idInsumo, double cantidad) throws SQLException {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        Insumo insumo = dao.obtenerPorId(idInsumo);
        if (insumo == null) throw new IllegalArgumentException("Insumo no encontrado: " + idInsumo);
        insumo.setStockActual(insumo.getStockActual() + cantidad);
        dao.actualizar(insumo);
    }

    // Borra un insumo por completo
    public void eliminar(int idInsumo) throws SQLException {
        dao.eliminar(idInsumo);
    }

    public List<Insumo> listarTodos()      throws SQLException { return dao.obtenerTodos(); }
    public Insumo       obtenerPorId(int id) throws SQLException { return dao.obtenerPorId(id); }

    // Revisa que el nombre, el stock y la medida sean correctos
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
