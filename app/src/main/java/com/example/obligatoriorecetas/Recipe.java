package com.example.obligatoriorecetas;

public class Recipe {
    public int id;
    public String name;
    public String category;
    public String area;
    public String instructions;
    public String ingredients;
    public String imageUrl;
    public String notes;

    public Recipe(int id, String name, String category, String area,
                  String instructions, String ingredients, String imageUrl,
                  String notes) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.area = area;
        this.instructions = instructions;
        this.ingredients = ingredients;
        this.imageUrl = imageUrl;
        this.notes = notes;
    }
}
