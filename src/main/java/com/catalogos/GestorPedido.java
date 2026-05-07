package com.catalogos;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Gestiona la lógica de negocio para pedidos.
 * Valida el detalle antes de crear un pedido e impone las
 * reglas de transición de estatus.
 */
public class GestorPedido {

    private final PedidoDAO    pedidoDAO;
    private final ProductoDAO  productoDAO;

    public GestorPedido() throws SQLException {
        this.pedidoDAO   = new PedidoDAO();
        this.productoDAO = new ProductoDAO();
    }

    /**
     * Crea un pedido completo tras validar que:
     * - El cliente existe (validación de negocio; la FK lo garantiza en BD).
     * - El detalle no está vacío.
     * - Cada producto existe y tiene precio >= 0.
     * La fecha se asigna automáticamente al día actual (ISO 8601).
     */
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
            // Captura el precio histórico al momento del pedido
            dp.setPrecioUnitario(p.getPrecioActual());
        }

        Pedido pedido = new Pedido(0, idCliente, LocalDate.now().toString(), "Pendiente");
        return pedidoDAO.insertar(pedido, detalle);
    }

    /**
     * Cambia el estatus de un pedido.
     * Solo permite la transición Pendiente → Entregado.
     */
    public void marcarEntregado(int idPedido) throws SQLException {
        Pedido p = pedidoDAO.obtenerPorId(idPedido);
        if (p == null) throw new IllegalArgumentException("Pedido no encontrado: " + idPedido);
        if ("Entregado".equals(p.getEstatus())) {
            throw new IllegalStateException("El pedido ya fue marcado como Entregado.");
        }
        pedidoDAO.actualizarEstatus(idPedido, "Entregado");
    }

    /** Elimina un pedido y su detalle (CASCADE en BD). */
    public void eliminar(int idPedido) throws SQLException {
        pedidoDAO.eliminar(idPedido);
    }

    public List<Pedido>       listarTodos()                    throws SQLException { return pedidoDAO.obtenerTodos(); }
    public List<Pedido>       listarPorCliente(int idCliente)  throws SQLException { return pedidoDAO.obtenerPorCliente(idCliente); }
    public List<Pedido>       listarPendientes()               throws SQLException { return pedidoDAO.obtenerPorEstatus("Pendiente"); }
    public List<Pedido>       listarEntregados()               throws SQLException { return pedidoDAO.obtenerPorEstatus("Entregado"); }
    public List<DetallePedido> obtenerDetalle(int idPedido)    throws SQLException { return pedidoDAO.obtenerDetalle(idPedido); }
    public Pedido             obtenerPorId(int id)             throws SQLException { return pedidoDAO.obtenerPorId(id); }
}
