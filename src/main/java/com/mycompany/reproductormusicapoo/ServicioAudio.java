package com.mycompany.reproductormusicapoo;

import javazoom.jl.player.Player;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

public class ServicioAudio {
    private Player player;
    private Thread hiloReproductor;
    
    // Variables de control de estado
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private String rutaActual = "";

    public void play(String rutaArchivo) {
        // 1. Si presionamos PLAY y es la MISMA canción que estaba pausada, solo quitamos la pausa
        if (isPaused && rutaArchivo.equals(rutaActual)) {
            isPaused = false;
            return;
        }

        // 2. Si es una canción nueva, detenemos todo y empezamos de cero
        stop();
        rutaActual = rutaArchivo;
        isPlaying = true;
        isPaused = false;

        hiloReproductor = new Thread(() -> {
            try {
                InputStream inputStream;
                
                // 🌐 Si es de la API
                if (rutaArchivo.startsWith("http://") || rutaArchivo.startsWith("https://")) {
                    URL url = new URL(rutaArchivo);
                    inputStream = url.openStream();
                } 
                // 📂 Si es Local
                else {
                    inputStream = new FileInputStream(rutaArchivo);
                }
                
                player = new Player(inputStream);
                
                // 3. EL TRUCO: Reproducir frame por frame (pedacito a pedacito)
                while (isPlaying && player != null) {
                    if (!isPaused) {
                        // Reproduce 1 frame. Si devuelve false, la canción terminó naturalemente
                        if (!player.play(1)) {
                            break; 
                        }
                    } else {
                        // Si está pausado, dormimos el hilo 50 milisegundos para no saturar el procesador
                        Thread.sleep(50);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error en reproducción: " + e.getMessage());
            }
        });
        hiloReproductor.start();
    }

    // Nuevo método dedicado exclusivamente a la pausa
    public void pause() {
        isPaused = true;
    }

    public void stop() {
        isPlaying = false;
        isPaused = false;
        rutaActual = "";
        
        if (player != null) {
            player.close();
        }
        if (hiloReproductor != null) {
            hiloReproductor.interrupt();
        }
    }
    public void seek(String rutaArchivo, int segundoDestino, int segundoMaximo) {
        stop(); // Detiene el hilo y el reproductor actual de golpe
        
        isPlaying = true;
        isPaused = false;
        rutaActual = rutaArchivo;

        hiloReproductor = new Thread(() -> {
            try {
                InputStream inputStream;
                long bytesASaltar = 0;

                // 🌐 Si la canción proviene de la API (Online)
                if (rutaArchivo.startsWith("http://") || rutaArchivo.startsWith("https://")) {
                    URL url = new URL(rutaArchivo);
                    inputStream = url.openStream();
                    // Una estimación estándar para streaming a 128kbps: ~16,000 bytes por segundo
                    bytesASaltar = (long) segundoDestino * 16000;
                } 
                // 📂 Si la canción es un archivo local
                else {
                    java.io.File archivo = new java.io.File(rutaArchivo);
                    long tamanoTotalArchivos = archivo.length();
                    // Regla de 3: Calculamos proporcionalmente cuántos bytes equivalen al segundo elegido
                    bytesASaltar = (tamanoTotalArchivos * segundoDestino) / segundoMaximo;
                    inputStream = new FileInputStream(archivo);
                }

                // 🦘 EL TRUCO: Saltamos los bytes calculados para llegar al punto exacto
                inputStream.skip(bytesASaltar);

                player = new Player(inputStream);

                // Continuamos con el bucle frame por frame que ya tenías
                while (isPlaying && player != null) {
                    if (!isPaused) {
                        if (!player.play(1)) {
                            break;
                        }
                    } else {
                        Thread.sleep(50);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error al realizar salto de posición: " + e.getMessage());
            }
        });
        hiloReproductor.start();
    }
}
// Método para saltar a un segundo específico de la canción
    