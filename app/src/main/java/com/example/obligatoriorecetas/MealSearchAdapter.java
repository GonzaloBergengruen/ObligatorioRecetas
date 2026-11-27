
package com.example.obligatoriorecetas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MealSearchAdapter extends RecyclerView.Adapter<MealSearchAdapter.MealHolder> {

    public interface OnMealClickListener {
        void onClick(MealSearchResult meal);
    }

    private final List<MealSearchResult> meals;
    private final OnMealClickListener listener;

    public MealSearchAdapter(List<MealSearchResult> meals, OnMealClickListener listener) {
        this.meals = meals;
        this.listener = listener;
    }

    @Override
    public MealHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_search, parent, false);

        return new MealHolder(item);
    }

    @Override
    public void onBindViewHolder(MealHolder holder, int position) {
        MealSearchResult meal = meals.get(position);

        holder.name.setText(meal.name);

        holder.itemView.setOnClickListener(view -> listener.onClick(meal));
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public static class MealHolder extends RecyclerView.ViewHolder {
        TextView name;

        public MealHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.mealSearchName);
        }
    }
}