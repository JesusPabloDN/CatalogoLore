package com.catalogos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Gestiona la relación N:M entre PRODUCTO e INSUMO (tabla PRODUCTO_INSUMO). */
public class ProductoInsumoDAO {

    private final Connection conn;

    public ProductoInsumoDAO() throws SQLException {
        this.conn = ConexionBD.getInstance().getConexion();
    }

    /** Asocia un insumo a un producto con su cantidad necesaria por unidad fabricada. */
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

    /** Devuelve todos los insumos requeridos para fabricar un producto dado. */
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

    /** Actualiza la cantidad necesaria de un insumo para un producto. */
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

    /** Elimina la asociación entre un producto y un insumo específico. */
    public void eliminar(int idProducto, int idInsumo) throws SQLException {
        String sql = "DELETE FROM PRODUCTO_INSUMO WHERE fk_producto = ? AND fk_insumo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idInsumo);
            ps.executeUpdate();
        }
    }

    /** Elimina todos los insumos asociados a un producto (útil al editar la receta completa). */
    public void eliminarPorProducto(int idProducto) throws SQLException {
        String sql = "DELETE FROM PRODUCTO_INSUMO WHERE fk_producto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.executeUpdate();
        }
    }
}
