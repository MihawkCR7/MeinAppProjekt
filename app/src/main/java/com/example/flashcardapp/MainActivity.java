package com.example.flashcardapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcardapp.model.AppDatabase;
import com.example.flashcardapp.model.Card;
import com.example.flashcardapp.model.CardDao;
import com.example.flashcardapp.model.Category;
import com.example.flashcardapp.model.CategoryDao;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button addCardButton;
    private Button manageCategoriesButton;
    private RecyclerView recyclerView;
    private CardAdapter cardAdapter;

    private Spinner categoryFilterSpinner; // Spinner für Kategorie-Filter
    private List<Category> categoryList = new ArrayList<>();

    private AppDatabase db;
    private CardDao cardDao;
    private CategoryDao categoryDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addCardButton = findViewById(R.id.button_add_card);
        manageCategoriesButton = findViewById(R.id.button_manage_categories);
        recyclerView = findViewById(R.id.recyclerView_cards);
        categoryFilterSpinner = findViewById(R.id.spinnerCategoryFilter); // Spinner initialisieren

        db = AppDatabase.getDatabase(getApplicationContext());
        cardDao = db.cardDao();
        categoryDao = db.categoryDao();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardAdapter = new CardAdapter(new ArrayList<>());
        recyclerView.setAdapter(cardAdapter);

        cardAdapter.setOnCardClickListener(card -> {
            Intent intent = new Intent(MainActivity.this, EditCardActivity.class);
            intent.putExtra("card_id", card.getId());
            startActivity(intent);
        });

        addCardButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditCardActivity.class);
            startActivity(intent);
        });

        manageCategoriesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CategoryManagementActivity.class);
            startActivity(intent);
        });

        insertSampleCard();

        loadCategoriesForSpinner(); // Kategorien für Spinner laden

        setupSpinnerListener(); // Spinner Listener setzen

        loadCards(); // initial alle Karten laden
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategoriesForSpinner(); // Kategorien aktuell halten
        loadCards(); // Karten neu laden
    }

    private void insertSampleCard() {
        new Thread(() -> {
            CategoryDao categoryDao = db.categoryDao();
            CardDao cardDao = db.cardDao();

            List<Category> categories = categoryDao.getAllCategories();

            int defaultCategoryId;

            if (categories.isEmpty()) {
                Category defaultCategory = new Category();
                defaultCategory.setName("Standard");

                categoryDao.insertCategory(defaultCategory);

                categories = categoryDao.getAllCategories();
            }

            if (!categories.isEmpty()) {
                defaultCategoryId = categories.get(0).getId();

                Card newCard = new Card();
                newCard.setQuestion("Was ist Android?");
                newCard.setAnswer("Ein Betriebssystem für mobile Geräte.");
                newCard.setBox(1);
                newCard.setCategoryId(defaultCategoryId);

                cardDao.insertCard(newCard);
            }
        }).start();
    }

    private void loadCards() {
        new Thread(() -> {
            List<Card> cards = cardDao.getAllCardsSorted();
            runOnUiThread(() -> cardAdapter.setCardList(cards));
        }).start();
    }

    private void loadCardsByCategory(int categoryId) {
        new Thread(() -> {
            List<Card> filteredCards = cardDao.getCardsByCategory(categoryId);
            runOnUiThread(() -> cardAdapter.setCardList(filteredCards));
        }).start();
    }

    private void loadCategoriesForSpinner() {
        new Thread(() -> {
            categoryList = categoryDao.getAllCategories();
            List<String> categoryNames = new ArrayList<>();
            categoryNames.add("Alle anzeigen");
            for (Category cat : categoryList) {
                categoryNames.add(cat.getName());
            }
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categoryFilterSpinner.setAdapter(adapter);
            });
        }).start();
    }

    private void setupSpinnerListener() {
        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    loadCards(); // Alle Karten anzeigen
                } else {
                    int selectedCategoryId = categoryList.get(position - 1).getId();
                    loadCardsByCategory(selectedCategoryId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadCards();
            }
        });
    }
}
