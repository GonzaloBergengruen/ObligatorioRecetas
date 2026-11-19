package com.example.obligatoriorecetas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecetaActivity extends AppCompatActivity {
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private RecipeDb db;
    private int recipeId;
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receta);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = new RecipeDb(this);
        recipeId = getIntent().getIntExtra("recipe_id", -1);

        findViewById(R.id.backButton).setOnClickListener(view -> finish());
        findViewById(R.id.editButton).setOnClickListener(view -> editRecipe());
        findViewById(R.id.deleteButton).setOnClickListener(view -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null) {
            loadRecipe();
        }
    }

    @Override
    protected void onDestroy() {
        imageExecutor.shutdown();
        super.onDestroy();
    }

    // Busca la receta en sqlite y presenta sus datos en la pantalla.
    private void loadRecipe() {
        recipe = db.get(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "No se encontro la receta", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.detailName)).setText(recipe.name);

        String category = recipe.category;
        if (category == null || category.isEmpty()) {
            category = "Receta";
        }

        String area = recipe.area;
        if (area == null || area.isEmpty()) {
            area = "";
        } else {
            area = " · " + area;
        }
        ((TextView) findViewById(R.id.detailCategory)).setText(category + area);

        ((TextView) findViewById(R.id.detailIngredients)).setText(valueOrDefault(recipe.ingredients, "Sin ingredientes detallados."));
        ((TextView) findViewById(R.id.detailInstructions)).setText(valueOrDefault(recipe.instructions, "Esta receta no tiene instrucciones."));
        ((TextView) findViewById(R.id.detailNotes)).setText(valueOrDefault(recipe.notes, "Todavia no agregaste notas."));

        loadImage();
    }

    // Descarga la imagen fuera del hilo principal para no bloquear la interfaz.
    private void loadImage() {
        ImageView imageView = findViewById(R.id.detailImage);
        if (recipe.imageUrl == null || recipe.imageUrl.isEmpty()) {
            imageView.setImageDrawable(null);
            imageView.setVisibility(android.view.View.GONE);
            return;
        }
        imageView.setVisibility(android.view.View.VISIBLE);
        String imageUrl = recipe.imageUrl;

        imageExecutor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                if (bitmap != null) {
                    mainHandler.post(() -> imageView.setImageBitmap(bitmap));
                }
            } catch (Exception ignored) {
                // La receta sigue disponible aunque no se pueda descargar su imagen.
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    // Abre el formulario con el ID para que cargue los datos existentes.
    private void editRecipe() {
        Intent intent = new Intent(this, RecipeFormActivity.class);
        intent.putExtra("recipe_id", recipeId);
        startActivity(intent);
    }

    // Pide confirmacion antes de eliminar la receta de la coleccion.
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar receta")
                .setMessage("¿Queres quitar esta receta de tu coleccion?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.delete(recipeId);
                    int lastRecipeId = getSharedPreferences("recetario", MODE_PRIVATE).getInt("last_recipe", -1);
                    if (lastRecipeId == recipeId) {
                        getSharedPreferences("recetario", MODE_PRIVATE)
                                .edit()
                                .remove("last_recipe")
                                .apply();
                    }
                    Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }
}
