package com.example.flashcardapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

    private int currentCardIndex = 0;

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
                // Keine Auswahl, evtl. alle Karten laden
                loadCardsForSelectedCategory(0);
            }
        });

        showAnswerButton.setOnClickListener(v -> {
            answerTextView.setVisibility(View.VISIBLE);
            showAnswerButton.setVisibility(View.GONE);
            knownButton.setEnabled(true);   // Buttons bei Antwort zeigen aktivieren
            unknownButton.setEnabled(true);
        });

        knownButton.setOnClickListener(v -> nextCard(true));

        unknownButton.setOnClickListener(v -> nextCard(false));

        // Anfangszustand: Buttons deaktiviert, Antwort ausgeblendet
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
        // Hier könntest du Fortschritt speichern (knewIt true/false)
        currentCardIndex++;
        if (currentCardIndex >= cardList.size()) {
            // Lernrunde fertig
            Toast.makeText(this, "Lernrunde abgeschlossen!", Toast.LENGTH_LONG).show();
            finish(); // Activity schließen oder Rückmeldung geben
        } else {
            showCard(cardList.get(currentCardIndex));
        }
    }
}
