package com.example.flashcardapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcardapp.model.AppDatabase;
import com.example.flashcardapp.model.Category;
import com.example.flashcardapp.model.CategoryDao;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categories = new ArrayList<>();
    private List<Category> fullCategoryList = new ArrayList<>();
    private CategoryDao categoryDao;
    private SearchView categorySearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        recyclerView = findViewById(R.id.recyclerView_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(categories, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onEdit(Category category) {
                showEditDialog(category);
            }

            @Override
            public void onDelete(Category category) {
                showDeleteConfirmation(category);
            }
        });

        recyclerView.setAdapter(adapter);

        categorySearchView = findViewById(R.id.searchView_categories);
        categorySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterCategories(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCategories(newText);
                return false;
            }
        });

        findViewById(R.id.button_add_category).setOnClickListener(v -> showAddDialog());

        categoryDao = AppDatabase.getDatabase(getApplicationContext()).categoryDao();

        loadCategories();
    }

    private void loadCategories() {
        new Thread(() -> {
            fullCategoryList = categoryDao.getAllCategories();
            runOnUiThread(() -> adapter.setCategoryList(fullCategoryList));
        }).start();
    }

    private void filterCategories(String text) {
        List<Category> filteredList = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            filteredList.addAll(fullCategoryList);
        } else {
            String lowerText = text.toLowerCase();
            for (Category c : fullCategoryList) {
                if (c.getName().toLowerCase().contains(lowerText)) {
                    filteredList.add(c);
                }
            }
        }
        adapter.setCategoryList(filteredList);
    }

    private void showAddDialog() {
        showCategoryDialog(null);
    }

    private void showEditDialog(Category category) {
        showCategoryDialog(category);
    }

    private void showCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(category == null ? "Neue Kategorie hinzufügen" : "Kategorie bearbeiten");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_category_edit, null);
        final EditText input = viewInflated.findViewById(R.id.editText_category_name);
        if (category != null) {
            input.setText(category.getName());
        }
        builder.setView(viewInflated);

        builder.setPositiveButton("Speichern", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name darf nicht leer sein", Toast.LENGTH_SHORT).show();
                return;
            }
            if (category == null) {
                addCategory(name);
            } else {
                updateCategory(category, name);
            }
        });

        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteConfirmation(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Kategorie löschen")
                .setMessage("Möchtest du die Kategorie \"" + category.getName() + "\" wirklich löschen?")
                .setPositiveButton("Ja", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Nein", null)
                .show();
    }

    private void addCategory(String name) {
        new Thread(() -> {
            Category newCategory = new Category();
            newCategory.setName(name);
            categoryDao.insertCategory(newCategory);
            runOnUiThread(this::loadCategories);
        }).start();
    }

    private void updateCategory(Category category, String newName) {
        new Thread(() -> {
            category.setName(newName);
            categoryDao.updateCategory(category);
            runOnUiThread(this::loadCategories);
        }).start();
    }

    private void deleteCategory(Category category) {
        new Thread(() -> {
            categoryDao.deleteCategory(category);
            runOnUiThread(this::loadCategories);
        }).start();
    }
}
