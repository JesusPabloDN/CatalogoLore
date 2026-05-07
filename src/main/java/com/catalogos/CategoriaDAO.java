package com.catalogos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD completo para la tabla CATEGORIA. */
public class CategoriaDAO {

    private final Connection conn;

    public CategoriaDAO() throws SQLException {
        this.conn = ConexionBD.getInstance().getConexion();
    }

    /** Inserta una nueva categoría y asigna el id generado al objeto. */
    public void insertar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO CATEGORIA (fk_datos, nombre) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, categoria.getFkDatos());
            ps.setString(2, categoria.getNombre());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) categoria.setIdCategoria(keys.getInt(1));
            }
        }
    }

    /** Devuelve todas las categorías ordenadas alfabéticamente. */
    public List<Categoria> obtenerTodas() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id_categoria, fk_datos, nombre FROM CATEGORIA ORDER BY nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Categoria(
                    rs.getInt("id_categoria"),
                    rs.getInt("fk_datos"),
                    rs.getString("nombre")
                ));
            }
        }
        return lista;
    }

    /** Busca una categoría por su id; retorna null si no existe. */
    public Categoria obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_categoria, fk_datos, nombre FROM CATEGORIA WHERE id_categoria = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(
                        rs.getInt("id_categoria"),
                        rs.getInt("fk_datos"),
                        rs.getString("nombre")
                    );
                }
            }
        }
        return null;
    }

    /** Actualiza el nombre de una categoría existente. */
    public void actualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE CATEGORIA SET nombre = ? WHERE id_categoria = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombre());
            ps.setInt(2, categoria.getIdCategoria());
            ps.executeUpdate();
        }
    }

    /** Elimina una categoría por id. Falla si tiene productos asociados (RESTRICT en BD). */
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM CATEGORIA WHERE id_categoria = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
