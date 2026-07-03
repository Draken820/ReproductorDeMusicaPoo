package com.mycompany.reproductormusicapoo.service;

import com.mycompany.reproductormusicapoo.model.Cancion;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de gestionar las peticiones asíncronas hacia la API de Deezer.
 */
public class MusicApiService {
    private final HttpClient httpClient;

    public MusicApiService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Realiza una búsqueda musical y notifica los resultados a través de un callback.
     */
    public void buscarCancionOnline(String query, ApiCallback callback) {
        String url = "https://api.deezer.com/search?q=" + query.replace(" ", "%20");
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (jsonObject.has("data") && jsonObject.getAsJsonArray("data").size() > 0) {
                        JsonArray dataArray = jsonObject.getAsJsonArray("data");
                        List<Cancion> resultados = new ArrayList<>();
                        
                        for (JsonElement element : dataArray) {
                            JsonObject track = element.getAsJsonObject();
                            String titulo = track.get("title").getAsString();
                            String artista = track.getAsJsonObject("artist").get("name").getAsString();
                            String previewUrl = track.get("preview").getAsString();
                            String albumCover = track.getAsJsonObject("album").get("cover_medium").getAsString();
                            resultados.add(new Cancion(titulo, artista, previewUrl, albumCover, true));
                        }
                        callback.onSuccess(resultados);
                    } else {
                        callback.onError("No se encontraron resultados.");
                    }
                })
                .exceptionally(ex -> {
                    callback.onError(ex.getMessage());
                    return null;
                });
    }

    /**
     * Interfaz para procesar las respuestas asíncronas de la API.
     */
    public interface ApiCallback {
        void onSuccess(List<Cancion> canciones);
        void onError(String error);
    }
}