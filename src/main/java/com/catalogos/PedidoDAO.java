package com.catalogos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Controla el guardado y consulta de Pedidos en la base de datos
public class PedidoDAO {

    private final Connection conn;

    public PedidoDAO() throws SQLException {
        this.conn = ConexionBD.getInstance().getConexion();
    }

    // --- Operaciones sobre PEDIDO ---

    // Guarda un nuevo pedido junto con todos sus productos
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

    // Obtiene una lista de los pedidos mostrando el nombre del cliente
    public List<String[]> obtenerTodosConNombre() throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT p.id_pedido, c.nombre_completo, p.fecha_pedido, p.estatus "
                   + "FROM PEDIDO p "
                   + "JOIN CLIENTE c ON p.fk_cliente = c.id_cliente "
                   + "ORDER BY p.fecha_pedido DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id_pedido")),
                    rs.getString("nombre_completo"),
                    rs.getString("fecha_pedido"),
                    rs.getString("estatus")
                });
            }
        }
        return lista;
    }

    // Obtiene una lista que junta la informacion del pedido y de sus productos
    public List<String[]> obtenerTodosFlattenado() throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT p.id_pedido, c.nombre_completo, p.fecha_pedido, p.estatus, "
                   + "       pr.nombre, dp.cantidad, dp.precio_unitario, "
                   + "       (dp.cantidad * dp.precio_unitario) AS subtotal "
                   + "FROM PEDIDO p "
                   + "JOIN CLIENTE c  ON p.fk_cliente    = c.id_cliente "
                   + "JOIN DETALLE_PEDIDO dp ON p.id_pedido   = dp.fk_pedido "
                   + "JOIN PRODUCTO pr ON dp.fk_producto  = pr.id_producto "
                   + "ORDER BY p.id_pedido DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id_pedido")),
                    rs.getString("nombre_completo"),
                    rs.getString("fecha_pedido"),
                    rs.getString("estatus"),
                    rs.getString("nombre"),
                    String.valueOf(rs.getInt("cantidad")),
                    String.format("$%.2f", rs.getDouble("precio_unitario")),
                    String.format("$%.2f", rs.getDouble("subtotal"))
                });
            }
        }
        return lista;
    }

    // Obtiene todos los pedidos ordenados de mas reciente a mas antiguo
    public List<Pedido> obtenerTodos() throws SQLException {
        String sql = "SELECT id_pedido, fk_cliente, fecha_pedido, estatus "
                   + "FROM PEDIDO ORDER BY fecha_pedido DESC";
        return ejecutarConsulta(sql);
    }

    // Busca todos los pedidos que pertenecen a un cliente
    public List<Pedido> obtenerPorCliente(int idCliente) throws SQLException {
        String sql = "SELECT id_pedido, fk_cliente, fecha_pedido, estatus "
                   + "FROM PEDIDO WHERE fk_cliente = ? ORDER BY fecha_pedido DESC";
        return ejecutarConsultaConParam(sql, idCliente);
    }

    // Busca los pedidos segun su estado actual
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

    // Busca un pedido utilizando su identificador
    public Pedido obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_pedido, fk_cliente, fecha_pedido, estatus "
                   + "FROM PEDIDO WHERE id_pedido = ?";
        List<Pedido> lista = ejecutarConsultaConParam(sql, id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    // Modifica el estado de un pedido
    public void actualizarEstatus(int idPedido, String nuevoEstatus) throws SQLException {
        String sql = "UPDATE PEDIDO SET estatus = ? WHERE id_pedido = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstatus);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        }
    }

    // Borra un pedido completo segun su identificador
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM PEDIDO WHERE id_pedido = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // --- Operaciones sobre DETALLE_PEDIDO ---

    // Obtiene todos los productos de un pedido especifico
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

    // Obtiene los productos de un pedido mostrando el nombre de cada uno
    public List<String[]> obtenerDetalleConNombre(int idPedido) throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT pr.nombre, dp.cantidad, dp.precio_unitario "
                   + "FROM DETALLE_PEDIDO dp "
                   + "JOIN PRODUCTO pr ON dp.fk_producto = pr.id_producto "
                   + "WHERE dp.fk_pedido = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double precio = rs.getDouble("precio_unitario");
                    int    cant   = rs.getInt("cantidad");
                    lista.add(new String[]{
                        rs.getString("nombre"),
                        String.valueOf(cant),
                        String.format("$%.2f", precio),
                        String.format("$%.2f", precio * cant)
                    });
                }
            }
        }
        return lista;
    }

    // --- Metodos auxiliares internos ---

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
