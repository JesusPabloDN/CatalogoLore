package com.catalogos;

import java.sql.SQLException;
import java.util.List;

/**
 * Gestiona la lógica de negocio para productos.
 * Valida reglas de negocio antes de llamar al DAO.
 */
public class GestorProducto {

    private final ProductoDAO dao;

    public GestorProducto() throws SQLException {
        this.dao = new ProductoDAO();
    }

    /**
     * Registra un producto nuevo tras validar:
     * - Nombre no vacío.
     * - Precio >= 0.
     * - Stock >= 0.
     */
    public void agregar(Producto producto) throws SQLException {
        validar(producto);
        dao.insertar(producto);
    }

    /** Actualiza todos los datos de un producto existente (mismas validaciones). */
    public void actualizar(Producto producto) throws SQLException {
        validar(producto);
        dao.actualizar(producto);
    }

    /**
     * Elimina un producto. La BD rechazará la operación si tiene
     * pedidos activos asociados (RESTRICT en DETALLE_PEDIDO).
     */
    public void eliminar(int idProducto) throws SQLException {
        dao.eliminar(idProducto);
    }

    /** Cambia la visibilidad del producto en el catálogo sin tocar otros campos. */
    public void cambiarDisponibilidad(int idProducto, boolean disponible) throws SQLException {
        Producto p = dao.obtenerPorId(idProducto);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado: " + idProducto);
        p.setDisponibilidad(disponible);
        dao.actualizar(p);
    }

    /** Incrementa el stock terminado de un producto. */
    public void agregarStock(int idProducto, int unidades) throws SQLException {
        if (unidades <= 0) throw new IllegalArgumentException("Las unidades deben ser mayores a 0.");
        Producto p = dao.obtenerPorId(idProducto);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado: " + idProducto);
        p.setStockTerminado(p.getStockTerminado() + unidades);
        dao.actualizar(p);
    }

    public List<Producto> listarTodos()                    throws SQLException { return dao.obtenerTodos(); }
    public List<Producto> listarPorCategoria(int idCat)   throws SQLException { return dao.obtenerPorCategoria(idCat); }
    public List<Producto> listarDisponibles()              throws SQLException { return dao.obtenerDisponibles(); }
    public Producto       obtenerPorId(int id)             throws SQLException { return dao.obtenerPorId(id); }

    // Validaciones mínimas de negocio
    private void validar(Producto p) {
        if (p.getNombre() == null || p.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");
        }
        if (p.getPrecioActual() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        if (p.getStockTerminado() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
    }
}
