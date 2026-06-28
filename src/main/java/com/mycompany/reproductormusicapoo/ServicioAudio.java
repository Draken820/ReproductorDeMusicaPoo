package com.mycompany.reproductormusicapoo;

import javazoom.jl.player.Player;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL; // ¡Importante para descargar el stream de internet!

public class ServicioAudio {
    private Player player;
    private Thread hiloReproductor;

    // Iniciar reproducción
    public void play(String rutaArchivo) {
        stop(); // Detenemos cualquier canción previa

        hiloReproductor = new Thread(() -> {
            try {
                InputStream inputStream;

                // 🌐 SI ES UN ENLACE DE INTERNET (API)
                if (rutaArchivo.startsWith("http://") || rutaArchivo.startsWith("https://")) {
                    URL url = new URL(rutaArchivo);
                    inputStream = url.openStream(); // Abre el flujo de audio desde la web
                } 
                // 📂 SI ES UN ARCHIVO LOCAL
                else {
                    inputStream = new FileInputStream(rutaArchivo);
                }
                
                player = new Player(inputStream);
                player.play();
            } catch (Exception e) {
                System.out.println("Error al reproducir el archivo: " + e.getMessage());
                e.printStackTrace();
            }
        });
        hiloReproductor.start(); 
    }

    // Detener reproducción
    public void stop() {
        if (player != null) {
            player.close();
        }
        if (hiloReproductor != null) {
            hiloReproductor.interrupt();
        }
    }
}