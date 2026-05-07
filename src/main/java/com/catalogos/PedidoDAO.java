package com.catalogos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD completo para las tablas PEDIDO y DETALLE_PEDIDO. */
public class PedidoDAO {

    private final Connection conn;

    public PedidoDAO() throws SQLException {
        this.conn = ConexionBD.getInstance().getConexion();
    }

    // -------------------------------------------------------------------------
    // Operaciones sobre PEDIDO

    /**
     * Inserta el encabezado del pedido y su detalle en una sola transacción.
     * Retorna el id generado para el pedido.
     */
    public int insertar(Pedido pedido, List<DetallePedido> detalle) throws SQLException {
        conn.setAutoCommit(false);
        try {
            int idPedido = insertarEncabezado(pedido);
            for (DetallePedido dp : detalle) {
                dp.setFkPedido(idPedido);
                insertarDetalle(dp);
            }
            conn.commit();
            return idPedido;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /** Devuelve todos los pedidos ordenados por fecha descendente. */
    public List<Pedido> obtenerTodos() throws SQLException {
        String sql = "SELECT id_pedido, fk_cliente, fecha_pedido, estatus "
                   + "FROM PEDIDO ORDER BY fecha_pedido DESC";
        return ejecutarConsulta(sql);
    }

    /** Filtra pedidos de un cliente específico. */
    public List<Pedido> obtenerPorCliente(int idCliente) throws SQLException {
        String sql = "SELECT id_pedido, fk_cliente, fecha_pedido, estatus "
                   + "FROM PEDIDO WHERE fk_cliente = ? ORDER BY fecha_pedido DESC";
        return ejecutarConsultaConParam(sql, idCliente);
    }

    /** Filtra pedidos por estatus ("Pendiente" o "Entregado"). */
    public List<Pedido> obtenerPorEstatus(String estatus) throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT id_pedido, fk_cliente, fecha_pedido, estatus "
                   + "FROM PEDIDO WHERE estatus = ? ORDER BY fecha_pedido DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estatus);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearPedido(rs));
            }
        }
        return lista;
    }

    /** Busca un pedido por id; retorna null si no existe. */
    public Pedido obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_pedido, fk_cliente, fecha_pedido, estatus "
                   + "FROM PEDIDO WHERE id_pedido = ?";
        List<Pedido> lista = ejecutarConsultaConParam(sql, id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Actualiza únicamente el estatus del pedido. */
    public void actualizarEstatus(int idPedido, String nuevoEstatus) throws SQLException {
        String sql = "UPDATE PEDIDO SET estatus = ? WHERE id_pedido = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstatus);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        }
    }

    /** Elimina un pedido y su detalle (CASCADE en BD). */
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM PEDIDO WHERE id_pedido = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Operaciones sobre DETALLE_PEDIDO

    /** Devuelve todos los renglones de un pedido dado. */
    public List<DetallePedido> obtenerDetalle(int idPedido) throws SQLException {
        List<DetallePedido> lista = new ArrayList<>();
        String sql = "SELECT fk_pedido, fk_producto, cantidad, precio_unitario "
                   + "FROM DETALLE_PEDIDO WHERE fk_pedido = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DetallePedido(
                        rs.getInt("fk_pedido"),
                        rs.getInt("fk_producto"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario")
                    ));
                }
            }
        }
        return lista;
    }

    // -------------------------------------------------------------------------
    // Métodos privados de apoyo

    private int insertarEncabezado(Pedido pedido) throws SQLException {
        String sql = "INSERT INTO PEDIDO (fk_cliente, fecha_pedido, estatus) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pedido.getFkCliente());
            ps.setString(2, pedido.getFechaPedido());
            ps.setString(3, pedido.getEstatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    pedido.setIdPedido(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se obtuvo el id generado para el pedido.");
    }

    private void insertarDetalle(DetallePedido dp) throws SQLException {
        String sql = "INSERT INTO DETALLE_PEDIDO "
                   + "(fk_pedido, fk_producto, cantidad, precio_unitario) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dp.getFkPedido());
            ps.setInt(2, dp.getFkProducto());
            ps.setInt(3, dp.getCantidad());
            ps.setDouble(4, dp.getPrecioUnitario());
            ps.executeUpdate();
        }
    }

    private List<Pedido> ejecutarConsulta(String sql) throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearPedido(rs));
        }
        return lista;
    }

    private List<Pedido> ejecutarConsultaConParam(String sql, int param) throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearPedido(rs));
            }
        }
        return lista;
    }

    private Pedido mapearPedido(ResultSet rs) throws SQLException {
        return new Pedido(
            rs.getInt("id_pedido"),
            rs.getInt("fk_cliente"),
            rs.getString("fecha_pedido"),
            rs.getString("estatus")
        );
    }
}
