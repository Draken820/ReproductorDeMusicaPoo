package com.mycompany.reproductormusicapoo.repository;

import com.mycompany.reproductormusicapoo.model.Cancion;
import java.util.List;

/**
 * Interfaz de repositorio para manejar las operaciones de acceso a datos de canciones.
 */
public interface CancionRepository {
    List<Cancion> obtenerPorGenero(String genero);
}