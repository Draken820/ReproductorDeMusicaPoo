package com.mycompany.reproductormusicapoo.model;

/**
 * Modelo de datos que representa una pista musical.
 */
public class Cancion {
    private final String titulo;
    private final String artista;
    private final String rutaArchivo;
    private final String rutaImagen;
    private final boolean esOnline;

    public Cancion(String titulo, String artista, String rutaArchivo, String rutaImagen, boolean esOnline) {
        this.titulo = titulo;
        this.artista = artista;
        this.rutaArchivo = rutaArchivo;
        this.rutaImagen = rutaImagen;
        this.esOnline = esOnline;
    }

    public String getTitulo() { return titulo; }
    public String getArtista() { return artista; }
    public String getRutaArchivo() { return rutaArchivo; }
    public String getRutaImagen() { return rutaImagen; }
    public boolean isOnline() { return esOnline; }
}