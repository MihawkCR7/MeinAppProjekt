package com.example.flashcardapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

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
    private Button manageCategoriesButton;  // Neuer Button
    private RecyclerView recyclerView;
    private CardAdapter cardAdapter;

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

        loadCards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCards();
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

                // Nach Insert: Liste neu laden, damit ID korrekt geladen wird
                categories = categoryDao.getAllCategories();
            }

            if (!categories.isEmpty()) {
                defaultCategoryId = categories.get(0).getId();

                // Karte nur einfügen, wenn Kategorie-ID gesetzt ist
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
}
