package com.catalogos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Controla el guardado y consulta de Categorias en la base de datos
public class CategoriaDAO {

    private final Connection conn;

    public CategoriaDAO() throws SQLException {
        this.conn = ConexionBD.getInstance().getConexion();
    }

    // Guarda una nueva categoria
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

    // Obtiene una lista con todas las categorias
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

    // Busca una categoria utilizando su identificador
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

    // Modifica los datos de una categoria
    public void actualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE CATEGORIA SET nombre = ? WHERE id_categoria = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombre());
            ps.setInt(2, categoria.getIdCategoria());
            ps.executeUpdate();
        }
    }

    // Revisa si ya existe una categoria con el mismo nombre
    public boolean existePorNombre(String nombre) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CATEGORIA WHERE LOWER(nombre) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Borra una categoria segun su identificador
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM CATEGORIA WHERE id_categoria = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
