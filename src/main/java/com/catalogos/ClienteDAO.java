package com.catalogos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Controla el guardado y consulta de Clientes en la base de datos
public class ClienteDAO {

    private final Connection conn;

    public ClienteDAO() throws SQLException {
        this.conn = ConexionBD.getInstance().getConexion();
    }

    // Guarda un nuevo cliente
    public void insertar(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO CLIENTE (nombre_completo, telefono, direccion_entrega) "
                   + "VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombreCompleto());
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getDireccionEntrega());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) cliente.setIdCliente(keys.getInt(1));
            }
        }
    }

    // Obtiene una lista con todos los clientes
    public List<Cliente> obtenerTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id_cliente, nombre_completo, telefono, direccion_entrega "
                   + "FROM CLIENTE ORDER BY nombre_completo";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre_completo"),
                    rs.getString("telefono"),
                    rs.getString("direccion_entrega")
                ));
            }
        }
        return lista;
    }

    // Busca un cliente utilizando su identificador
    public Cliente obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_cliente, nombre_completo, telefono, direccion_entrega "
                   + "FROM CLIENTE WHERE id_cliente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Cliente(
                        rs.getInt("id_cliente"),
                        rs.getString("nombre_completo"),
                        rs.getString("telefono"),
                        rs.getString("direccion_entrega")
                    );
                }
            }
        }
        return null;
    }

    // Modifica los datos de un cliente
    public void actualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE CLIENTE SET nombre_completo = ?, telefono = ?, "
                   + "direccion_entrega = ? WHERE id_cliente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombreCompleto());
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getDireccionEntrega());
            ps.setInt(4, cliente.getIdCliente());
            ps.executeUpdate();
        }
    }

    // Borra un cliente segun su identificador
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM CLIENTE WHERE id_cliente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
