package com.catalogos;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// Revisa que los pedidos y sus productos esten bien antes de guardarlos
public class GestorPedido {

    private final PedidoDAO    pedidoDAO;
    private final ProductoDAO  productoDAO;

    public GestorPedido() throws SQLException {
        this.pedidoDAO   = new PedidoDAO();
        this.productoDAO = new ProductoDAO();
    }

    // Revisa los datos y guarda un nuevo pedido con la fecha de hoy
    public int crear(int idCliente, List<DetallePedido> detalle) throws SQLException {
        if (detalle == null || detalle.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto.");
        }
        for (DetallePedido dp : detalle) {
            if (dp.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a 0.");
            }
            Producto p = productoDAO.obtenerPorId(dp.getFkProducto());
            if (p == null) {
                throw new IllegalArgumentException("Producto no encontrado: " + dp.getFkProducto());
            }
            // Guarda el precio actual del producto
            dp.setPrecioUnitario(p.getPrecioActual());
        }

        Pedido pedido = new Pedido(0, idCliente, LocalDate.now().toString(), "Pendiente");
        return pedidoDAO.insertar(pedido, detalle);
    }

    // Cambia un pedido a estado Entregado
    public void marcarEntregado(int idPedido) throws SQLException {
        Pedido p = pedidoDAO.obtenerPorId(idPedido);
        if (p == null) throw new IllegalArgumentException("Pedido no encontrado: " + idPedido);
        if ("Entregado".equals(p.getEstatus())) {
            throw new IllegalStateException("El pedido ya fue marcado como Entregado.");
        }
        pedidoDAO.actualizarEstatus(idPedido, "Entregado");
    }

    // Borra un pedido completo y sus productos
    public void eliminar(int idPedido) throws SQLException {
        pedidoDAO.eliminar(idPedido);
    }

    public List<Pedido>        listarTodos()                    throws SQLException { return pedidoDAO.obtenerTodos(); }
    public List<String[]>      listarConNombres()               throws SQLException { return pedidoDAO.obtenerTodosConNombre(); }
    public List<String[]>      listarFlattenado()               throws SQLException { return pedidoDAO.obtenerTodosFlattenado(); }
    public List<Pedido>        listarPorCliente(int idCliente)  throws SQLException { return pedidoDAO.obtenerPorCliente(idCliente); }
    public List<Pedido>        listarPendientes()               throws SQLException { return pedidoDAO.obtenerPorEstatus("Pendiente"); }
    public List<Pedido>        listarEntregados()               throws SQLException { return pedidoDAO.obtenerPorEstatus("Entregado"); }
    public List<DetallePedido> obtenerDetalle(int idPedido)     throws SQLException { return pedidoDAO.obtenerDetalle(idPedido); }
    public List<String[]>      obtenerDetalleConNombre(int id)  throws SQLException { return pedidoDAO.obtenerDetalleConNombre(id); }
    public Pedido              obtenerPorId(int id)             throws SQLException { return pedidoDAO.obtenerPorId(id); }
}
