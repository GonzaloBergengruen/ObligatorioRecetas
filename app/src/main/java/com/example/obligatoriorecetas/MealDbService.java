
package com.example.obligatoriorecetas;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MealDbService {

    private static final String BASE = "https://www.themealdb.com/api/json/v1/1/";

    private static String request(String endpoint) throws Exception {
        URL url = new URL(BASE + endpoint);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        try {
            InputStream input = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            return response.toString();

        } finally {
            connection.disconnect();
        }
    }

    //Buscar recetas por nombre
    public static List<MealSearchResult> buscarNombre(String nombre) throws Exception {
        String parametro = URLEncoder.encode(nombre, "UTF-8");

        JSONObject json = new JSONObject(request("search.php?s=" + parametro));

        return parseResultados(json);
    }

    //Buscar recetas por categoría
    public static List<MealSearchResult> buscarCategoria(String categoria) throws Exception {
        String parametro = URLEncoder.encode(categoria, "UTF-8");

        JSONObject json = new JSONObject(request("filter.php?c=" + parametro));

        return parseResultados(json);
    }

    //Buscar recetas por área
    public static List<MealSearchResult> buscarArea(String area) throws Exception {
        String parametro = URLEncoder.encode(area, "UTF-8");

        JSONObject json = new JSONObject(request("filter.php?a=" + parametro));

        return parseResultados(json);
    }

    //Obtener todos los detalles de una receta
    public static Recipe obtenerDetalle(String externalId) throws Exception {
        JSONObject json = new JSONObject(request("lookup.php?i=" + externalId));

        JSONArray meals = json.optJSONArray("meals");

        if (meals == null || meals.length() == 0) {
            return null;
        }

        JSONObject item = meals.getJSONObject(0);

        String nombre = item.optString("strMeal");
        String categoria = item.optString("strCategory");
        String area = item.optString("strArea");
        String instrucciones = item.optString("strInstructions");
        String imagen = item.optString("strMealThumb");

        StringBuilder ingredientes = new StringBuilder();

        for (int i = 1; i <= 20; i++) {
            String ingrediente = item.optString("strIngredient" + i, "").trim();
            String medida = item.optString("strMeasure" + i, "").trim();

            if (!ingrediente.isEmpty()) {
                if (ingredientes.length() > 0) {
                    ingredientes.append("\n");
                }
                if (!medida.isEmpty()) {
                    ingredientes.append(medida).append(" ");
                }
                ingredientes.append(ingrediente);
            }
        }

        //id = 0 para que SQLite genere un ID local.
        return new Recipe(
                0,
                nombre,
                categoria,
                area,
                instrucciones,
                ingredientes.toString(),
                imagen,
                ""
        );
    }

    //Obtener categorías para el Spinner
    public static List<String> obtenerCategorias() throws Exception {
        return obtenerLista("list.php?c=list", "strCategory");
    }

    //Obtener áreas para el Spinner
    public static List<String> obtenerAreas() throws Exception {
        return obtenerLista("list.php?a=list", "strArea");
    }

    private static List<String> obtenerLista(String endpoint, String campo) throws Exception {
        JSONObject json = new JSONObject(request(endpoint));

        JSONArray array = json.optJSONArray("meals");

        List<String> lista = new ArrayList<>();

        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                lista.add(array.getJSONObject(i).optString(campo));
            }
        }

        return lista;
    }
    
    public static List<MealSearchResult> buscarTodas() throws Exception {
        //Evita duplicados y conserva el orden.
        java.util.LinkedHashMap<String, MealSearchResult> todas = new java.util.LinkedHashMap<>();

        String letras = "abcdefghijklmnopqrstuvwxyz";

        for (int i = 0; i < letras.length(); i++) {
            char letra = letras.charAt(i);
            JSONObject json = new JSONObject(request("search.php?f=" + letra));

            List<MealSearchResult> resultados = parseResultados(json);

            for (MealSearchResult meal : resultados) {
                todas.put(meal.externalId, meal);
            }
        }

        return new ArrayList<>(todas.values());
    }


    private static List<MealSearchResult> parseResultados(JSONObject json) throws Exception {
        List<MealSearchResult> resultados = new ArrayList<>();

        JSONArray meals = json.optJSONArray("meals");

        if (meals == null) {
            return resultados;
        }

        for (int i = 0; i < meals.length(); i++) {
            JSONObject item = meals.getJSONObject(i);

            resultados.add(new MealSearchResult(
                    item.optString("idMeal"),
                    item.optString("strMeal"),
                    item.optString("strMealThumb")
            ));
        }

        return resultados;
    }
}