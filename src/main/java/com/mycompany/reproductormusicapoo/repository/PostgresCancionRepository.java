package com.mycompany.reproductormusicapoo.repository;

import com.mycompany.reproductormusicapoo.model.Cancion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del repositorio de canciones utilizando una base de datos PostgreSQL.
 */
public class PostgresCancionRepository implements CancionRepository {
    private final String URL = "jdbc:postgresql://localhost:5432/lol";
    private final String USER = "postgres";
    private final String PASS = "";

    /**
     * Consulta y devuelve las canciones disponibles en la base de datos según el género indicado.
     */
    @Override   
    public List<Cancion> obtenerPorGenero(String genero) {
        List<Cancion> canciones = new ArrayList<>();
        String sql = "SELECT titulo, artista, ruta_archivo, ruta_imagen FROM canciones_locales WHERE genero = ?";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, genero);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                canciones.add(new Cancion(
                    rs.getString("titulo"),
                    rs.getString("artista"),
                    rs.getString("ruta_archivo"),
                    rs.getString("ruta_imagen"),
                    false
                ));
            }
        } catch (Exception e) {
            System.out.println("Error de Base de Datos (Cargando lista vacía): " + e.getMessage());
        }
        return canciones;
    }
}