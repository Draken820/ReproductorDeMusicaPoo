/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reproductormusicapoo;

/**
 *
 * @author drako
 */
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MusicApiService {
    private final HttpClient httpClient;

    public MusicApiService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    // Método para buscar una canción online de manera asíncrona
    public void buscarCancionOnline(String query, ApiCallback callback) {
        // Ejemplo usando la API pública de Deezer
        String url = "https://api.deezer.com/search?q=" + query.replace(" ", "%20");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    // Parsear el JSON usando Gson
                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (jsonObject.has("data") && jsonObject.getAsJsonArray("data").size() > 0) {
                        JsonObject primeraCancion = jsonObject.getAsJsonArray("data").get(0).getAsJsonObject();
                        
                        String titulo = primeraCancion.get("title").getAsString();
                        String artista = primeraCancion.getAsJsonObject("artist").get("name").getAsString();
                        String previewUrl = primeraCancion.get("preview").getAsString(); // URL del audio stream
                        String albumCover = primeraCancion.getAsJsonObject("album").get("cover_medium").getAsString();

                        Cancion cancionOnline = new Cancion(titulo, artista, previewUrl, albumCover, true);
                        callback.onSuccess(cancionOnline);
                    } else {
                        callback.onError("No se encontraron resultados en la API.");
                    }
                })
                .exceptionally(ex -> {
                    callback.onError(ex.getMessage());
                    return null;
                });
    }

    // Interfaz para manejar la respuesta asíncrona sin bloquear la UI
    public interface ApiCallback {
        void onSuccess(Cancion cancion);
        void onError(String error);
    }
}
