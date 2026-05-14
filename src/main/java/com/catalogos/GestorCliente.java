package com.catalogos;

import java.sql.SQLException;
import java.util.List;

// Revisa que los datos del cliente sean correctos antes de guardarlos
public class GestorCliente {

    private final ClienteDAO dao;

    public GestorCliente() throws SQLException {
        this.dao = new ClienteDAO();
    }

    // Revisa los datos y guarda un nuevo cliente
    public void agregar(String nombreCompleto, String telefono,
                        String direccionEntrega) throws SQLException {
        validar(nombreCompleto, telefono);
        Cliente cliente = new Cliente(0, nombreCompleto.trim(), telefono, direccionEntrega);
        dao.insertar(cliente);
    }

    // Revisa los datos y modifica a un cliente
    public void actualizar(Cliente cliente) throws SQLException {
        validar(cliente.getNombreCompleto(), cliente.getTelefono());
        dao.actualizar(cliente);
    }

    // Borra a un cliente si no tiene pedidos registrados
    public void eliminar(int idCliente) throws SQLException {
        dao.eliminar(idCliente);
    }

    public List<Cliente> listarTodos()        throws SQLException { return dao.obtenerTodos(); }
    public Cliente       obtenerPorId(int id) throws SQLException { return dao.obtenerPorId(id); }

    // Revisa que el nombre exista y el telefono tenga 10 numeros
    private void validar(String nombre, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío.");
        }
        if (telefono == null || !telefono.matches("\\d{10}")) {
            throw new IllegalArgumentException("El teléfono debe tener exactamente 10 dígitos numéricos.");
        }
    }
}
