
package com.example.obligatoriorecetas;

public class MealSearchResult {

    public String externalId;
    public String name;
    public String imageUrl;

    public MealSearchResult(String externalId, String name, String imageUrl) {
        this.externalId = externalId;
        this.name = name;
        this.imageUrl = imageUrl;
    }
}