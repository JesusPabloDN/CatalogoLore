package com.catalogos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Controla el guardado y consulta de Productos en la base de datos
public class ProductoDAO {

    private final Connection conn;

    public ProductoDAO() throws SQLException {
        this.conn = ConexionBD.getInstance().getConexion();
    }

    // Guarda un nuevo producto
    public void insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO PRODUCTO "
                   + "(fk_categoria, nombre, beneficios, precio_actual, ruta_imagen, "
                   + " disponibilidad, stock_terminado) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getFkCategoria());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getBeneficios());
            ps.setDouble(4, p.getPrecioActual());
            ps.setString(5, p.getRutaImagen());
            ps.setInt(6, p.isDisponibilidad() ? 1 : 0);
            ps.setInt(7, p.getStockTerminado());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setIdProducto(keys.getInt(1));
            }
        }
    }

    // Obtiene una lista con todos los productos
    public List<Producto> obtenerTodos() throws SQLException {
        String sql = "SELECT * FROM PRODUCTO ORDER BY nombre";
        return ejecutarConsulta(sql);
    }

    // Busca los productos que pertenecen a una categoria
    public List<Producto> obtenerPorCategoria(int idCategoria) throws SQLException {
        String sql = "SELECT * FROM PRODUCTO WHERE fk_categoria = ? ORDER BY nombre";
        return ejecutarConsultaConParam(sql, idCategoria);
    }

    // Busca los productos que estan marcados para mostrarse
    public List<Producto> obtenerDisponibles() throws SQLException {
        String sql = "SELECT * FROM PRODUCTO WHERE disponibilidad = 1 ORDER BY nombre";
        return ejecutarConsulta(sql);
    }

    // Busca un producto utilizando su identificador
    public Producto obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM PRODUCTO WHERE id_producto = ?";
        List<Producto> lista = ejecutarConsultaConParam(sql, id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    // Modifica todos los datos de un producto
    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE PRODUCTO SET fk_categoria = ?, nombre = ?, beneficios = ?, "
                   + "precio_actual = ?, ruta_imagen = ?, disponibilidad = ?, "
                   + "stock_terminado = ? WHERE id_producto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getFkCategoria());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getBeneficios());
            ps.setDouble(4, p.getPrecioActual());
            ps.setString(5, p.getRutaImagen());
            ps.setInt(6, p.isDisponibilidad() ? 1 : 0);
            ps.setInt(7, p.getStockTerminado());
            ps.setInt(8, p.getIdProducto());
            ps.executeUpdate();
        }
    }

    // Borra un producto segun su identificador
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM PRODUCTO WHERE id_producto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // --- Metodos auxiliares internos ---

    private List<Producto> ejecutarConsulta(String sql) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private List<Producto> ejecutarConsultaConParam(String sql, int param) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // Transforma los datos de la base de datos en un Producto
    private Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(
            rs.getInt("id_producto"),
            rs.getInt("fk_categoria"),
            rs.getString("nombre"),
            rs.getString("beneficios"),
            rs.getDouble("precio_actual"),
            rs.getString("ruta_imagen"),
            rs.getInt("disponibilidad") == 1,
            rs.getInt("stock_terminado")
        );
    }
}
