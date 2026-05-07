package com.catalogos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD completo para la tabla INSUMO. */
public class InsumoDAO {

    private final Connection conn;

    public InsumoDAO() throws SQLException {
        this.conn = ConexionBD.getInstance().getConexion();
    }

    /** Inserta un insumo y asigna el id generado al objeto. */
    public void insertar(Insumo insumo) throws SQLException {
        String sql = "INSERT INTO INSUMO (nombre, stock_actual, unidad_medida) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, insumo.getNombre());
            ps.setDouble(2, insumo.getStockActual());
            ps.setString(3, insumo.getUnidadMedida());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) insumo.setIdInsumo(keys.getInt(1));
            }
        }
    }

    /** Devuelve todos los insumos ordenados por nombre. */
    public List<Insumo> obtenerTodos() throws SQLException {
        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT id_insumo, nombre, stock_actual, unidad_medida "
                   + "FROM INSUMO ORDER BY nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Insumo(
                    rs.getInt("id_insumo"),
                    rs.getString("nombre"),
                    rs.getDouble("stock_actual"),
                    rs.getString("unidad_medida")
                ));
            }
        }
        return lista;
    }

    /** Busca un insumo por id; retorna null si no existe. */
    public Insumo obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_insumo, nombre, stock_actual, unidad_medida "
                   + "FROM INSUMO WHERE id_insumo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Insumo(
                        rs.getInt("id_insumo"),
                        rs.getString("nombre"),
                        rs.getDouble("stock_actual"),
                        rs.getString("unidad_medida")
                    );
                }
            }
        }
        return null;
    }

    /** Actualiza nombre, stock y unidad de medida de un insumo. */
    public void actualizar(Insumo insumo) throws SQLException {
        String sql = "UPDATE INSUMO SET nombre = ?, stock_actual = ?, unidad_medida = ? "
                   + "WHERE id_insumo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, insumo.getNombre());
            ps.setDouble(2, insumo.getStockActual());
            ps.setString(3, insumo.getUnidadMedida());
            ps.setInt(4, insumo.getIdInsumo());
            ps.executeUpdate();
        }
    }

    /** Elimina un insumo por id. Falla si tiene recetas asociadas (CASCADE lo maneja en PRODUCTO_INSUMO). */
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM INSUMO WHERE id_insumo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
