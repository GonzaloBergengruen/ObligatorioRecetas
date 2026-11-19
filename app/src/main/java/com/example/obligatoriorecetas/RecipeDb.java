package com.example.obligatoriorecetas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class RecipeDb extends SQLiteOpenHelper {
    public RecipeDb(Context context) {
        super(context, "mi_recetario.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE recipes (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, category TEXT, area TEXT, " +
                "instructions TEXT, ingredients TEXT, image_url TEXT, notes TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public int save(Recipe recipe) {
        ContentValues values = new ContentValues();
        values.put("name", recipe.name);
        values.put("category", recipe.category);
        values.put("area", recipe.area);
        values.put("instructions", recipe.instructions);
        values.put("ingredients", recipe.ingredients);
        values.put("image_url", recipe.imageUrl);
        values.put("notes", recipe.notes);

        if (recipe.id > 0) {
            getWritableDatabase().update("recipes", values, "_id=?",
                    new String[]{String.valueOf(recipe.id)});
            return recipe.id;
        }
        long insertedId = getWritableDatabase().insert("recipes", null, values);
        return (int) insertedId;
    }

    public List<Recipe> all() {
        List<Recipe> recipes = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM recipes ORDER BY name COLLATE NOCASE", null)) {
            while (cursor.moveToNext()) {
                recipes.add(readRecipe(cursor));
            }
        }
        return recipes;
    }

    public Recipe get(int id) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM recipes WHERE _id=?",
                new String[]{String.valueOf(id)})) {
            if (cursor.moveToFirst()) {
                return readRecipe(cursor);
            }
        }
        return null;
    }

    public void delete(int id) {
        getWritableDatabase().delete(
                "recipes",
                "_id=?",
                new String[]{String.valueOf(id)}
        );
    }

    private Recipe readRecipe(Cursor cursor) {
        return new Recipe(cursor.getInt(0), cursor.getString(1),
                cursor.getString(2), cursor.getString(3), cursor.getString(4),
                cursor.getString(5), cursor.getString(6), cursor.getString(7));
    }
}
