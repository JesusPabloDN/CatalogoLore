package com.catalogos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Consulta y actualiza la configuracion del negocio en la base de datos
public class DatosCatalogoDAO {

    private final Connection conn;

    public DatosCatalogoDAO() throws SQLException {
        this.conn = ConexionBD.getInstance().getConexion();
    }

    // Obtiene la informacion del negocio
    public DatosCatalogo obtener() throws SQLException {
        String sql = "SELECT id_datos, nombre_negocio, descripcion, telefono_contacto "
                   + "FROM DATOS_CATALOGO WHERE id_datos = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new DatosCatalogo(
                    rs.getInt("id_datos"),
                    rs.getString("nombre_negocio"),
                    rs.getString("descripcion"),
                    rs.getString("telefono_contacto")
                );
            }
        }
        return null;
    }

    // Actualiza la informacion del negocio
    public void actualizar(DatosCatalogo datos) throws SQLException {
        String sql = "UPDATE DATOS_CATALOGO SET nombre_negocio = ?, descripcion = ?, "
                   + "telefono_contacto = ? WHERE id_datos = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, datos.getNombreNegocio());
            ps.setString(2, datos.getDescripcion());
            ps.setString(3, datos.getTelefonoContacto());
            ps.executeUpdate();
        }
    }
}
