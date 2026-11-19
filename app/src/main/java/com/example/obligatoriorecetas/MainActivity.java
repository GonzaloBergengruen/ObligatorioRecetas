package com.example.obligatoriorecetas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Base de datos local, adaptador de la lista y recetas.
    private RecipeDb db;
    private RecipeAdapter adapter;
    private final List<Recipe> visibleRecipes = new ArrayList<>();
    private Spinner categoryFilter;

    // Firebase para comprobar y cerrar la sesion del usuario.
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Comprueba la sesion de Firebase antes de mostrar la coleccion.
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            openLogin();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = new RecipeDb(this);
        // Prepara las acciones, el filtro y la lista que muestra las recetas guardadas.
        setupButtons();
        setupFilter();
        setupRecipeList();
        showLastRecipe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualiza la lista al volver desde el formulario o desde otra pantalla.
        if (db != null && categoryFilter != null) {
            loadRecipes(categoryFilter.getSelectedItem().toString());
            showLastRecipe();
        }
    }

    private void setupButtons() {
        // Abre el formulario vacio para crear una receta propia.
        findViewById(R.id.newRecipeButton).setOnClickListener(view -> openForm());
        // Abre la pantalla para buscar recetas externas.
        findViewById(R.id.searchButton).setOnClickListener(view -> startActivity(new Intent(this, SearchActivity.class)));
        // Cierra la sesion de Firebase y regresa al login.
        findViewById(R.id.logoutButton).setOnClickListener(view -> {
            firebaseAuth.signOut();
            openLogin();
        });
    }

    private void setupFilter() {
        // Llena el Spinner con las categorias disponibles.
        categoryFilter = findViewById(R.id.categoryFilter);
        String[] categories = {"Todas", "Entrada", "Principal", "Postre", "Bebida"};
        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );
        categoryFilter.setAdapter(categoriesAdapter);
        categoryFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                // Cada vez que cambia la seleccion, vuelve a cargar la lista.
                loadRecipes(categories[position]);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupRecipeList() {
        // Configura el RecyclerView para mostrar las recetas en una columna.
        RecyclerView recipeList = findViewById(R.id.recipeList);
        recipeList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(visibleRecipes, recipeId -> openDetail(recipeId));
        recipeList.setAdapter(adapter);
    }

    private void loadRecipes(String category) {
        // Vacia la lista visible y agrega todas las recetas que coinciden con la categoría.
        visibleRecipes.clear();
        for (Recipe recipe : db.all()) {
            if (category.equals("Todas") || category.equalsIgnoreCase(recipe.category)) {
                visibleRecipes.add(recipe);
            }
        }
        if (adapter != null) {
            // Avisa al RecyclerView que debe volver a dibujar sus filas.
            adapter.notifyDataSetChanged();
        }
    }

    private void showLastRecipe() {
        // Recupera el ID guardado por el formulario o la busqueda y muestra esa receta.
        TextView recent = findViewById(R.id.recentLabel);
        int lastId = getSharedPreferences("recetario", MODE_PRIVATE)
                .getInt("last_recipe", -1);
        Recipe recipe = db.get(lastId);
        if (recipe != null) {
            recent.setVisibility(View.VISIBLE);
            recent.setText("TU ULTIMA RECETA   ->   " + recipe.name);
            recent.setOnClickListener(view -> openDetail(recipe.id));
        } else {
            recent.setVisibility(View.GONE);
        }
    }

    private void openDetail(int recipeId) {
        // Envia el ID de la receta elegida a la pantalla de detalle.
        Intent intent = new Intent(this, RecetaActivity.class);
        intent.putExtra("recipe_id", recipeId);
        startActivity(intent);
    }

    private void openForm() {
        // Abre la pantalla donde se crea una receta.
        startActivity(new Intent(this, RecipeFormActivity.class));
    }

    private void openLogin() {
        // Limpia la navegacion para que Atras no regrese a la coleccion sin sesion.
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private interface RecipeClickListener {
        // accion que ejecuta una fila cuando el usuario la toca.
        void onClick(int recipeId);
    }

    // Adaptador que convierte cada Recipe en una fila del RecyclerView.
    private static class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeHolder> {
        private final List<Recipe> recipes;
        private final RecipeClickListener clickListener;

        RecipeAdapter(List<Recipe> recipes, RecipeClickListener clickListener) {
            this.recipes = recipes;
            this.clickListener = clickListener;
        }

        @Override
        public RecipeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Infla el XML de una fila individual de receta (item_recipe.xml).
            View item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recipe, parent, false);
            return new RecipeHolder(item);
        }

        @Override
        public void onBindViewHolder(RecipeHolder holder, int position) {
            // Coloca en la fila los datos de la receta correspondiente.
            Recipe recipe = recipes.get(position);
            holder.name.setText(recipe.name);
            if (recipe.category == null || recipe.category.isEmpty()) {
                holder.category.setText("Receta");
            } else {
                holder.category.setText(recipe.category);
            }
            holder.itemView.setOnClickListener(view -> clickListener.onClick(recipe.id));
        }

        @Override
        public int getItemCount() {
            // Informa al RecyclerView cuantas recetas debe mostrar.
            return recipes.size();
        }

        // Conserva las referencias a las vistas de una fila para reutilizarlas.
        private static class RecipeHolder extends RecyclerView.ViewHolder {
            private final TextView name;
            private final TextView category;

            RecipeHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.recipeName);
                category = itemView.findViewById(R.id.recipeMeta);
            }
        }
    }
}
