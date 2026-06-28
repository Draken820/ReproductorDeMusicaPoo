/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reproductormusicapoo;

/**
 *
 * @author drako
 */
public class Cancion {
    private final String titulo;
    private final String artista;
    private final String rutaArchivo; // Ruta local (MP3) o URL de la API
    private final String rutaImagen;  // Imagen de portada local o remota
    private final boolean esOnline;

    public Cancion(String titulo, String artista, String rutaArchivo, String rutaImagen, boolean esOnline) {
        this.titulo = titulo;
        this.artista = artista;
        this.rutaArchivo = rutaArchivo;
        this.rutaImagen = rutaImagen;
        this.esOnline = esOnline;
    }

    // Getters
    public String getTitulo() { return titulo; }
    public String getArtista() { return artista; }
    public String getRutaArchivo() { return rutaArchivo; }
    public String getRutaImagen() { return rutaImagen; }
    public boolean isOnline() { return esOnline; }
}