package com.catalogos;

import java.sql.SQLException;
import java.util.List;

// Revisa que los datos de los productos esten bien antes de guardarlos
public class GestorProducto {

    private final ProductoDAO dao;

    public GestorProducto() throws SQLException {
        this.dao = new ProductoDAO();
    }

    // Revisa los datos y guarda un nuevo producto
    public void agregar(Producto producto) throws SQLException {
        validar(producto);
        dao.insertar(producto);
    }

    // Revisa los datos y modifica un producto
    public void actualizar(Producto producto) throws SQLException {
        validar(producto);
        dao.actualizar(producto);
    }

    // Borra un producto si no esta en un pedido
    public void eliminar(int idProducto) throws SQLException {
        try {
            dao.eliminar(idProducto);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se puede eliminar: el producto tiene pedidos asociados.", e);
        }
    }

    // Oculta o muestra un producto en el PDF
    public void cambiarDisponibilidad(int idProducto, boolean disponible) throws SQLException {
        Producto p = dao.obtenerPorId(idProducto);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado: " + idProducto);
        p.setDisponibilidad(disponible);
        dao.actualizar(p);
    }

    // Le suma una cantidad al stock de un producto terminado
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

    // Revisa que el nombre, el precio y el stock sean correctos
    private void validar(Producto p) {
        if (p.getNombre() == null || p.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");
        }
        // Revisa que se haya elegido una categoria
        if (p.getFkCategoria() <= 0) {
            throw new IllegalArgumentException("Debes seleccionar una categoría.");
        }
        if (p.getPrecioActual() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        if (p.getStockTerminado() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        // Si no le pusieron beneficios se guarda vacio
        if (p.getBeneficios() == null) {
            p.setBeneficios("");
        }
    }
}
