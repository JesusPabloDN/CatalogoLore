package com.catalogos;

import java.sql.SQLException;
import java.util.List;

// Revisa los datos de los ingredientes antes de agregarlos a una receta
public class GestorIngrediente {

    private final ProductoInsumoDAO productoInsumoDAO;
    private final InsumoDAO         insumoDAO;

    public GestorIngrediente() throws SQLException {
        this.productoInsumoDAO = new ProductoInsumoDAO();
        this.insumoDAO         = new InsumoDAO();
    }

    // Revisa los datos y agrega un ingrediente al producto
    public void agregar(int idProducto, int idInsumo, double cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad necesaria debe ser mayor a 0.");
        }
        if (insumoDAO.obtenerPorId(idInsumo) == null) {
            throw new IllegalArgumentException("El insumo indicado no existe.");
        }
        if (productoInsumoDAO.existeIngrediente(idProducto, idInsumo)) {
            throw new IllegalArgumentException("Este insumo ya fue agregado a ese producto.");
        }
        productoInsumoDAO.insertar(new ProductoInsumo(idProducto, idInsumo, cantidad));
    }

    // Modifica la cantidad que se usa de un ingrediente
    public void actualizar(int idProducto, int idInsumo, double nuevaCantidad) throws SQLException {
        if (nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad necesaria debe ser mayor a 0.");
        }
        productoInsumoDAO.actualizar(new ProductoInsumo(idProducto, idInsumo, nuevaCantidad));
    }

    // Quita un ingrediente de la receta
    public void eliminar(int idProducto, int idInsumo) throws SQLException {
        productoInsumoDAO.eliminar(idProducto, idInsumo);
    }

    // Borra la receta anterior y guarda la nueva
    public void reemplazarReceta(int idProducto, List<ProductoInsumo> nuevaReceta) throws SQLException {
        productoInsumoDAO.eliminarPorProducto(idProducto);
        for (ProductoInsumo pi : nuevaReceta) {
            if (pi.getCantidadNecesaria() <= 0) {
                throw new IllegalArgumentException("Cantidad inválida en la receta.");
            }
            productoInsumoDAO.insertar(pi);
        }
    }

    // Obtiene la lista de ingredientes de un producto
    public List<ProductoInsumo> listarPorProducto(int idProducto) throws SQLException {
        return productoInsumoDAO.obtenerPorProducto(idProducto);
    }

    // Obtiene los ingredientes con sus nombres para mostrarlos en pantalla
    public List<String[]> listarPorProductoConNombre(int idProducto) throws SQLException {
        return productoInsumoDAO.obtenerPorProductoConNombre(idProducto);
    }
}
