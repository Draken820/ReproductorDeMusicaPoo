/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reproductormusicapoo;

/**
 *
 * @author drako
 */
import java.util.ArrayList;
import java.util.List;

public class LocalMusicaRepositorio {

    public List<Cancion> getPlaylistByGenero(String genero) {
        List<Cancion> canciones = new ArrayList<>();
        
        switch (genero.toLowerCase()) {
            case "rock":
                // Fíjate en la nueva ruta: "src/main/java/assets/..."
                canciones.add(new Cancion("In the End", "Linkin Park", "src/main/java/assets/audio/rock1.mp3", "src/main/java/assets/images/rock1.jpg", false));
                canciones.add(new Cancion("The Pretender", "Foo Fighters", "src/main/java/assets/audio/rock2.mp3", "src/main/java/assets/images/rock2.jpg", false));
                canciones.add(new Cancion("Tercera Cancion", "Artista", "src/main/java/assets/audio/rock3.mp3", "src/main/java/assets/images/rock3.jpg", false));
                break;
                
            case "pop":
                canciones.add(new Cancion("Blinding Lights", "The Weeknd", "src/main/java/assets/audio/pop1.mp3", "src/main/java/assets/images/pop1.jpg", false));
                canciones.add(new Cancion("As It Was", "Harry Styles", "src/main/java/assets/audio/pop2.mp3", "src/main/java/assets/images/pop2.jpg", false));
                break;
                
            case "anime":
                canciones.add(new Cancion("A Cruel Angel's Thesis", "Yoko Takahashi", "src/main/java/assets/audio/anime1.mp3", "src/main/java/assets/images/evangelion.jpg", false));
                canciones.add(new Cancion("Departure!", "Masatoshi Ono", "src/main/java/assets/audio/anime2.mp3", "src/main/java/assets/images/hxh.jpg", false));
                canciones.add(new Cancion("Bloody Stream", "Coda", "src/main/java/assets/audio/anime3.mp3", "src/main/java/assets/images/jojo.jpg", false));
                break;
        }
        return canciones;
    }
}