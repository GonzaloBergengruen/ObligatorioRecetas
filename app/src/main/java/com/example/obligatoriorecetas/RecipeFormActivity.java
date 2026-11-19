package com.example.obligatoriorecetas;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RecipeFormActivity extends AppCompatActivity {
    private RecipeDb db;
    private Recipe recipeToEdit;
    private EditText nameInput;
    private EditText ingredientsInput;
    private EditText instructionsInput;
    private EditText notesInput;
    private Spinner categorySpinner;
    private Spinner areaSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_form);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.formRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = new RecipeDb(this);
        connectViews();
        setupSpinners();
        loadRecipeToEdit();

        findViewById(R.id.formSave).setOnClickListener(view -> saveRecipe());
        findViewById(R.id.formCancel).setOnClickListener(view -> finish());
    }

    // Vincula los campos del formulario con sus variables.
    private void connectViews() {
        nameInput = findViewById(R.id.formName);
        ingredientsInput = findViewById(R.id.formIngredients);
        instructionsInput = findViewById(R.id.formInstructions);
        notesInput = findViewById(R.id.formNotes);
        categorySpinner = findViewById(R.id.formCategory);
        areaSpinner = findViewById(R.id.formArea);
    }

    // Carga las opciones que el usuario puede elegir para categoria y area.
    private void setupSpinners() {
        String[] categories = {"Entrada", "Principal", "Postre", "Bebida"};
        String[] areas = {
                "Uruguay", "Argentina", "Italian", "Mexican", "Japanese",
                "Indian", "American", "British", "Canadian", "Chinese",
                "French", "Greek", "Spanish", "Thai", "Turkish", "Otra"
        };

        categorySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));
        areaSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, areas));
    }

    // Si se recibio el ID de una receta, busca sus datos y completa el formulario.
    private void loadRecipeToEdit() {
        int recipeId = getIntent().getIntExtra("recipe_id", -1);
        if (recipeId < 0) {
            return;
        }

        recipeToEdit = db.get(recipeId);
        if (recipeToEdit == null) {
            Toast.makeText(this, "No se encontro la receta", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView title = findViewById(R.id.formTitle);
        title.setText("Editar receta");
        nameInput.setText(recipeToEdit.name);
        ingredientsInput.setText(recipeToEdit.ingredients);
        instructionsInput.setText(recipeToEdit.instructions);
        notesInput.setText(recipeToEdit.notes);
        selectOption(categorySpinner, recipeToEdit.category);
        selectOption(areaSpinner, recipeToEdit.area);
    }

    // Valida el nombre, crea o actualiza la receta y guarda el ID de la ultima modificada.
    private void saveRecipe() {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            nameInput.setError("El nombre de la receta es obligatorio");
            nameInput.requestFocus();
            return;
        }

        Recipe recipe;
        if (recipeToEdit == null) {
            recipe = new Recipe(0, "", "", "", "", "", "", "");
        } else {
            recipe = recipeToEdit;
        }

        recipe.name = name;
        recipe.category = categorySpinner.getSelectedItem().toString();
        recipe.area = areaSpinner.getSelectedItem().toString();
        recipe.ingredients = ingredientsInput.getText().toString().trim();
        recipe.instructions = instructionsInput.getText().toString().trim();
        recipe.notes = notesInput.getText().toString().trim();

        int savedId = db.save(recipe);
        if (savedId < 0) {
            Toast.makeText(this, "No se pudo guardar la receta", Toast.LENGTH_LONG).show();
            return;
        }

        getSharedPreferences("recetario", MODE_PRIVATE)
                .edit()
                .putInt("last_recipe", savedId)
                .apply();

        Toast.makeText(this, "Receta guardada", Toast.LENGTH_SHORT).show();
        finish();
    }

    // Selecciona en un Spinner la opcion que coincide con el dato guardado.
    private void selectOption(Spinner spinner, String value) {
        if (value == null) {
            return;
        }

        for (int index = 0; index < spinner.getCount(); index++) {
            String option = spinner.getItemAtPosition(index).toString();
            if (option.equalsIgnoreCase(value)) {
                spinner.setSelection(index);
                return;
            }
        }
    }
}
