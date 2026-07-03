package com.mycompany.reproductormusicapoo.service;

import javazoom.jl.player.Player;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.lang.reflect.Field;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

/**
 * Clase encargada de gestionar la reproducción de audio, incluyendo
 * funciones como reproducir, pausar, detener, saltar a un segmento y ajustar el volumen.
 */
public class ServicioAudio {
    private Player player;
    private Thread hiloReproductor;

    private boolean isPlaying = false;
    private boolean isPaused = false;
    private String rutaActual = "";
    private int volumenActual = 100;

    /**
     * Inicia o reanuda la reproducción de una pista de audio local o remota.
     */
    public void play(String rutaArchivo) {
        if (isPaused && rutaArchivo.equals(rutaActual)) {
            isPaused = false;
            return;
        }

        stop();
        rutaActual = rutaArchivo;
        isPlaying = true;
        isPaused = false;

        hiloReproductor = new Thread(() -> {
            try {
                InputStream inputStream;
                
                if (rutaArchivo.startsWith("http://") || rutaArchivo.startsWith("https://")) {
                    URL url = new URL(rutaArchivo);
                    inputStream = url.openStream();
                } else {
                    inputStream = new FileInputStream(rutaArchivo);
                }
                
                player = new Player(inputStream);
                boolean primeraVez = true;
                
                while (isPlaying && player != null) {
                    if (!isPaused) {
                        if (!player.play(1)) {
                            break; 
                        }
                        
                        if (primeraVez) {
                            setVolumen(volumenActual);
                            primeraVez = false;
                        }
                    } else {
                        Thread.sleep(50);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error en reproducción: " + e.getMessage());
            }
        });
        hiloReproductor.start();
    }

    /**
     * Pausa la reproducción actual del audio.
     */
    public void pause() {
        isPaused = true;
    }

    /**
     * Detiene completamente el audio y reinicia el estado de reproducción.
     */
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

    /**
     * Permite adelantar o atrasar la reproducción a un segundo específico de la canción.
     */
    public void seek(String rutaArchivo, int segundoDestino, int segundoMaximo) {
        stop();
        isPlaying = true;
        isPaused = false;
        rutaActual = rutaArchivo;

        hiloReproductor = new Thread(() -> {
            try {
                InputStream inputStream;
                long bytesASaltar = 0;

                if (rutaArchivo.startsWith("http://") || rutaArchivo.startsWith("https://")) {
                    URL url = new URL(rutaArchivo);
                    inputStream = url.openStream();
                    bytesASaltar = (long) segundoDestino * 16000;
                } else {
                    java.io.File archivo = new java.io.File(rutaArchivo);
                    long tamanoTotal = archivo.length();
                    bytesASaltar = (tamanoTotal * segundoDestino) / segundoMaximo;
                    inputStream = new FileInputStream(archivo);
                }

                inputStream.skip(bytesASaltar);
                player = new Player(inputStream);
                boolean primeraVez = true;

                while (isPlaying && player != null) {
                    if (!isPaused) {
                        if (!player.play(1)) {
                            break;
                        }
                        if (primeraVez) {
                            setVolumen(volumenActual);
                            primeraVez = false;
                        }
                    } else {
                        Thread.sleep(50);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error al realizar salto: " + e.getMessage());
            }
        });
        hiloReproductor.start();
    }

    /**
     * Ajusta los decibelios del sistema usando la línea de datos del reproductor.
     */
    public void setVolumen(int porcentaje) {
        this.volumenActual = porcentaje;
        if (player != null) {
            try {
                Field fieldAudio = player.getClass().getDeclaredField("audio");
                fieldAudio.setAccessible(true);
                Object audioDevice = fieldAudio.get(player);

                if (audioDevice instanceof javazoom.jl.player.JavaSoundAudioDevice) {
                    Field fieldLine = audioDevice.getClass().getDeclaredField("source");
                    fieldLine.setAccessible(true);
                    SourceDataLine line = (SourceDataLine) fieldLine.get(audioDevice);
                    
                    if (line != null && line.isOpen() && line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                        float db;
                        
                        if (porcentaje <= 0) {
                            db = control.getMinimum();
                        } else {
                            db = (float) (Math.log10(porcentaje / 100.0) * 20.0);
                        }

                        control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), db)));
                    }
                }
            } catch (Exception e) {
                System.out.println("Error controlando volumen interno: " + e.getMessage());
            }
        }
    }
}