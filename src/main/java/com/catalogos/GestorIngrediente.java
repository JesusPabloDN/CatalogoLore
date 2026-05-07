package com.catalogos;

import java.sql.SQLException;
import java.util.List;

/**
 * Gestiona los ingredientes (insumos) de cada producto.
 * Opera sobre la tabla PRODUCTO_INSUMO sin tocar SQL directamente.
 */
public class GestorIngrediente {

    private final ProductoInsumoDAO productoInsumoDAO;
    private final InsumoDAO         insumoDAO;

    public GestorIngrediente() throws SQLException {
        this.productoInsumoDAO = new ProductoInsumoDAO();
        this.insumoDAO         = new InsumoDAO();
    }

    /**
     * Agrega un insumo como ingrediente de un producto, validando que:
     * - La cantidad sea mayor a 0.
     * - El insumo exista en la BD.
     */
    public void agregar(int idProducto, int idInsumo, double cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad necesaria debe ser mayor a 0.");
        }
        if (insumoDAO.obtenerPorId(idInsumo) == null) {
            throw new IllegalArgumentException("El insumo indicado no existe.");
        }
        productoInsumoDAO.insertar(new ProductoInsumo(idProducto, idInsumo, cantidad));
    }

    /** Actualiza la cantidad de un ingrediente ya asociado al producto. */
    public void actualizar(int idProducto, int idInsumo, double nuevaCantidad) throws SQLException {
        if (nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad necesaria debe ser mayor a 0.");
        }
        productoInsumoDAO.actualizar(new ProductoInsumo(idProducto, idInsumo, nuevaCantidad));
    }

    /** Elimina un ingrediente específico de la receta de un producto. */
    public void eliminar(int idProducto, int idInsumo) throws SQLException {
        productoInsumoDAO.eliminar(idProducto, idInsumo);
    }

    /**
     * Reemplaza la receta completa de un producto.
     * Útil cuando se edita la lista entera en la UI de una sola vez.
     */
    public void reemplazarReceta(int idProducto, List<ProductoInsumo> nuevaReceta) throws SQLException {
        productoInsumoDAO.eliminarPorProducto(idProducto);
        for (ProductoInsumo pi : nuevaReceta) {
            if (pi.getCantidadNecesaria() <= 0) {
                throw new IllegalArgumentException("Cantidad inválida en la receta.");
            }
            productoInsumoDAO.insertar(pi);
        }
    }

    /** Devuelve los ingredientes de un producto dado. */
    public List<ProductoInsumo> listarPorProducto(int idProducto) throws SQLException {
        return productoInsumoDAO.obtenerPorProducto(idProducto);
    }
}
