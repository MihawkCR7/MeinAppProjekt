package com.example.flashcardapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flashcardapp.model.AppDatabase;
import com.example.flashcardapp.model.Card;
import com.example.flashcardapp.model.CardDao;
import com.example.flashcardapp.model.Category;
import com.example.flashcardapp.model.CategoryDao;

import java.util.ArrayList;
import java.util.List;

public class LearningActivity extends AppCompatActivity {

    private Spinner categorySelectorSpinner;
    private TextView questionTextView;
    private TextView answerTextView;
    private Button showAnswerButton;
    private Button knownButton;
    private Button unknownButton;

    private AppDatabase db;
    private CardDao cardDao;
    private CategoryDao categoryDao;

    private List<Category> categoryList = new ArrayList<>();
    private List<Card> cardList = new ArrayList<>();
    private List<Card> repeatCards = new ArrayList<>();

    private int currentCardIndex = 0;
    private int knownCount = 0;
    private int unknownCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        db = AppDatabase.getDatabase(getApplicationContext());
        cardDao = db.cardDao();
        categoryDao = db.categoryDao();

        categorySelectorSpinner = findViewById(R.id.spinnerCategorySelector);
        questionTextView = findViewById(R.id.textView_question);
        answerTextView = findViewById(R.id.textView_answer);
        showAnswerButton = findViewById(R.id.button_show_answer);
        knownButton = findViewById(R.id.button_known);
        unknownButton = findViewById(R.id.button_unknown);

        loadCategories();

        categorySelectorSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadCardsForSelectedCategory(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                loadCardsForSelectedCategory(0);
            }
        });

        showAnswerButton.setOnClickListener(v -> {
            answerTextView.setVisibility(View.VISIBLE);
            showAnswerButton.setVisibility(View.GONE);
            knownButton.setEnabled(true);
            unknownButton.setEnabled(true);
        });

        knownButton.setOnClickListener(v -> nextCard(true));
        unknownButton.setOnClickListener(v -> nextCard(false));

        knownButton.setEnabled(false);
        unknownButton.setEnabled(false);
    }

    private void loadCategories() {
        new Thread(() -> {
            categoryList = categoryDao.getAllCategories();
            runOnUiThread(() -> {
                List<String> categoryNames = new ArrayList<>();
                categoryNames.add("Alle Karten");
                for (Category category : categoryList) {
                    categoryNames.add(category.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySelectorSpinner.setAdapter(adapter);
            });
        }).start();
    }

    private void loadCardsForSelectedCategory(int spinnerPosition) {
        new Thread(() -> {
            if (spinnerPosition == 0) {
                cardList = cardDao.getAllCardsSorted();
            } else {
                int categoryId = categoryList.get(spinnerPosition - 1).getId();
                cardList = cardDao.getCardsByCategory(categoryId);
            }

            currentCardIndex = 0;
            knownCount = 0;
            unknownCount = 0;
            repeatCards.clear();

            runOnUiThread(() -> {
                if (cardList.isEmpty()) {
                    Toast.makeText(LearningActivity.this, "Keine Karten in dieser Kategorie", Toast.LENGTH_SHORT).show();
                    questionTextView.setText("");
                    answerTextView.setText("");
                    showAnswerButton.setVisibility(View.GONE);
                    knownButton.setEnabled(false);
                    unknownButton.setEnabled(false);
                    return;
                }
                showCard(cardList.get(currentCardIndex));
            });
        }).start();
    }

    private void showCard(Card card) {
        questionTextView.setText(card.getQuestion());
        answerTextView.setText(card.getAnswer());
        answerTextView.setVisibility(View.GONE);
        showAnswerButton.setVisibility(View.VISIBLE);
        knownButton.setEnabled(false);
        unknownButton.setEnabled(false);
    }

    private void nextCard(boolean knewIt) {
        if (knewIt) {
            knownCount++;
        } else {
            unknownCount++;
            repeatCards.add(cardList.get(currentCardIndex)); // Nicht gewusste Karte zur Wiederholung vormerken
        }

        currentCardIndex++;
        if (currentCardIndex >= cardList.size()) {
            // Lernrunde abgeschlossen → Zusammenfassung und ggf. Wiederholung
            String summary = "Du hast " + knownCount + " von " + cardList.size() + " Karten gewusst (" +
                    (cardList.size() > 0 ? (100 * knownCount / cardList.size()) : 0) + "%)." +
                    "\nNicht gewusst: " + unknownCount;

            if (!repeatCards.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("Lernfortschritt")
                        .setMessage(summary + "\n\nNicht gewusste Karten erneut lernen?")
                        .setPositiveButton("Ja", (dialog, which) -> repeatLernrunde())
                        .setNegativeButton("Nein", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Lernfortschritt")
                        .setMessage(summary)
                        .setPositiveButton("OK", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }
        } else {
            showCard(cardList.get(currentCardIndex));
        }
    }

    private void repeatLernrunde() {
        cardList = new ArrayList<>(repeatCards);
        repeatCards.clear();
        currentCardIndex = 0;
        knownCount = 0;
        unknownCount = 0;
        if (!cardList.isEmpty()) {
            showCard(cardList.get(currentCardIndex));
        }
    }
}
