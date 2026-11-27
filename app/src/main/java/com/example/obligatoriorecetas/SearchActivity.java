
package com.example.obligatoriorecetas;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

    private RecipeDb db;

    private EditText searchInput;
    private Spinner categorySpinner;
    private Spinner areaSpinner;

    private RecyclerView recyclerResults;
    private TextView emptyLabel;

    private MealSearchAdapter adapter;

    private final List<MealSearchResult> results = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = new RecipeDb(this);

        searchInput = findViewById(R.id.searchInput);
        categorySpinner = findViewById(R.id.searchCategory);
        areaSpinner = findViewById(R.id.searchArea);
        recyclerResults = findViewById(R.id.searchResults);
        emptyLabel = findViewById(R.id.searchStatus);

        recyclerResults.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MealSearchAdapter(results, this::showMealDetails);

        recyclerResults.setAdapter(adapter);

        findViewById(R.id.searchSubmit).setOnClickListener(view -> searchMeals());

        findViewById(R.id.searchBack).setOnClickListener(view -> finish());

        loadFilters();
    }

    private void loadFilters() {
        executor.execute(() -> {
            try {
                List<String> categories = MealDbService.obtenerCategorias();
                List<String> areas = MealDbService.obtenerAreas();

                categories.add(0, "Todas");
                areas.add(0, "Todas");

                mainHandler.post(() -> {
                    setSpinnerAdapter(categorySpinner, categories);
                    setSpinnerAdapter(areaSpinner, areas);
                });
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "Error al cargar categorías y áreas", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setSpinnerAdapter(Spinner spinner, List<String> values) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(spinnerAdapter);
    }

    private void searchMeals() {
        String name = searchInput.getText().toString().trim();

        String category = categorySpinner.getSelectedItem().toString();

        String area = areaSpinner.getSelectedItem().toString();

        executor.execute(() -> {
            try {
                List<MealSearchResult> found;

                if (!name.isEmpty()) {
                    found = MealDbService.buscarNombre(name);
                } else if (!category.equals("Todas")) {
                    found = MealDbService.buscarCategoria(category);
                } else if (!area.equals("Todas")) {
                    found = MealDbService.buscarArea(area);
                } else {
                    mainHandler.post(() -> { emptyLabel.setText("Cargando todas las recetas..."); emptyLabel.setVisibility(View.VISIBLE); });

                    found = MealDbService.buscarTodas();
                }

                mainHandler.post(() -> {
                    results.clear();
                    results.addAll(found);

                    adapter.notifyDataSetChanged();

                    if (results.isEmpty()) {
                        emptyLabel.setText("No se encontraron recetas.");
                        emptyLabel.setVisibility(View.VISIBLE);
                    } else {
                        emptyLabel.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "No se pudo realizar la búsqueda", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showMealDetails(MealSearchResult result) {
        executor.execute(() -> {
            try {
                Recipe recipe = MealDbService.obtenerDetalle(result.externalId);

                if (recipe == null) {
                    return;
                }

                mainHandler.post(() -> {
                    String message =
                            "Categoría: " + recipe.category +
                                    "\nÁrea: " + recipe.area +
                                    "\n\nIngredientes:\n" +
                                    recipe.ingredients +
                                    "\n\nInstrucciones:\n" +
                                    recipe.instructions;

                    new AlertDialog.Builder(this)
                            .setTitle(recipe.name)
                            .setMessage(message)
                            .setNegativeButton(
                                    "Cerrar", null)
                            .setPositiveButton(
                                    "Agregar a mi recetario",
                                    (dialog, which) ->
                                            saveExternalRecipe(recipe))
                            .show();
                });
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "No se pudo cargar el detalle", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveExternalRecipe(Recipe recipe) {
        //Evita guardar dos recetas con el mismo nombre.
        for (Recipe saved : db.all()) {
            if (saved.name != null && saved.name.equalsIgnoreCase(recipe.name)) {
                Toast.makeText(this, "Esta receta ya está en tu colección", Toast.LENGTH_SHORT).show();

                return;
            }
        }

        int savedId = db.save(recipe);

        if (savedId > 0) {
            getSharedPreferences("recetario", MODE_PRIVATE).edit().putInt("last_recipe", savedId).apply();

            Toast.makeText(this, "Receta agregada a tu recetario", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No se pudo guardar la receta", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}