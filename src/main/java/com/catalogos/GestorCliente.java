package com.catalogos;

import java.sql.SQLException;
import java.util.List;

/**
 * Gestiona la lógica de negocio para clientes.
 * Valida que el teléfono tenga exactamente 10 dígitos numéricos.
 */
public class GestorCliente {

    private final ClienteDAO dao;

    public GestorCliente() throws SQLException {
        this.dao = new ClienteDAO();
    }

    /**
     * Registra un cliente nuevo tras validar:
     * - Nombre no vacío.
     * - Teléfono con exactamente 10 dígitos numéricos.
     */
    public void agregar(String nombreCompleto, String telefono,
                        String direccionEntrega) throws SQLException {
        validar(nombreCompleto, telefono);
        Cliente cliente = new Cliente(0, nombreCompleto.trim(), telefono, direccionEntrega);
        dao.insertar(cliente);
    }

    /** Actualiza los datos de un cliente existente (mismas validaciones). */
    public void actualizar(Cliente cliente) throws SQLException {
        validar(cliente.getNombreCompleto(), cliente.getTelefono());
        dao.actualizar(cliente);
    }

    /**
     * Elimina un cliente. La BD rechazará la operación si tiene
     * pedidos asociados (RESTRICT en PEDIDO.fk_cliente).
     */
    public void eliminar(int idCliente) throws SQLException {
        dao.eliminar(idCliente);
    }

    public List<Cliente> listarTodos()        throws SQLException { return dao.obtenerTodos(); }
    public Cliente       obtenerPorId(int id) throws SQLException { return dao.obtenerPorId(id); }

    // Valida nombre y formato de teléfono (10 dígitos exactos)
    private void validar(String nombre, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío.");
        }
        if (telefono == null || !telefono.matches("\\d{10}")) {
            throw new IllegalArgumentException("El teléfono debe tener exactamente 10 dígitos numéricos.");
        }
    }
}
