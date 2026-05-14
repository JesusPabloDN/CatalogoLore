package com.catalogos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Controla la lista de ingredientes que lleva cada producto
public class ProductoInsumoDAO {

    private final Connection conn;

    public ProductoInsumoDAO() throws SQLException {
        this.conn = ConexionBD.getInstance().getConexion();
    }

    // Guarda un ingrediente para un producto
    public void insertar(ProductoInsumo pi) throws SQLException {
        String sql = "INSERT INTO PRODUCTO_INSUMO (fk_producto, fk_insumo, cantidad_necesaria) "
                   + "VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pi.getFkProducto());
            ps.setInt(2, pi.getFkInsumo());
            ps.setDouble(3, pi.getCantidadNecesaria());
            ps.executeUpdate();
        }
    }

    // Revisa si el producto ya tiene agregado ese ingrediente
    public boolean existeIngrediente(int idProducto, int idInsumo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PRODUCTO_INSUMO "
                   + "WHERE fk_producto = ? AND fk_insumo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idInsumo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Obtiene todos los ingredientes de un producto
    public List<ProductoInsumo> obtenerPorProducto(int idProducto) throws SQLException {
        List<ProductoInsumo> lista = new ArrayList<>();
        String sql = "SELECT fk_producto, fk_insumo, cantidad_necesaria "
                   + "FROM PRODUCTO_INSUMO WHERE fk_producto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ProductoInsumo(
                        rs.getInt("fk_producto"),
                        rs.getInt("fk_insumo"),
                        rs.getDouble("cantidad_necesaria")
                    ));
                }
            }
        }
        return lista;
    }

    // Obtiene los ingredientes mostrando sus nombres y unidades
    public List<String[]> obtenerPorProductoConNombre(int idProducto) throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT pi.fk_insumo, i.nombre, i.unidad_medida, pi.cantidad_necesaria "
                   + "FROM PRODUCTO_INSUMO pi "
                   + "JOIN INSUMO i ON pi.fk_insumo = i.id_insumo "
                   + "WHERE pi.fk_producto = ? "
                   + "ORDER BY i.nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                        String.valueOf(rs.getInt("fk_insumo")),
                        rs.getString("nombre") + " (" + rs.getString("unidad_medida") + ")",
                        String.valueOf(rs.getDouble("cantidad_necesaria"))
                    });
                }
            }
        }
        return lista;
    }

    // Modifica la cantidad que se usa de un ingrediente
    public void actualizar(ProductoInsumo pi) throws SQLException {
        String sql = "UPDATE PRODUCTO_INSUMO SET cantidad_necesaria = ? "
                   + "WHERE fk_producto = ? AND fk_insumo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, pi.getCantidadNecesaria());
            ps.setInt(2, pi.getFkProducto());
            ps.setInt(3, pi.getFkInsumo());
            ps.executeUpdate();
        }
    }

    // Quita un ingrediente de la receta de un producto
    public void eliminar(int idProducto, int idInsumo) throws SQLException {
        String sql = "DELETE FROM PRODUCTO_INSUMO WHERE fk_producto = ? AND fk_insumo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idInsumo);
            ps.executeUpdate();
        }
    }

    // Borra todos los ingredientes de un producto
    public void eliminarPorProducto(int idProducto) throws SQLException {
        String sql = "DELETE FROM PRODUCTO_INSUMO WHERE fk_producto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.executeUpdate();
        }
    }
}
