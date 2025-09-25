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
    private int repeatKnownCount = 0;
    private int repeatUnknownCount = 0;

    private boolean repeatModus = false;

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
                repeatModus = false; // Hauptlernmodus
                loadDueCardsForCategory(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                repeatModus = false;
                loadDueCardsForCategory(0);
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

    private void loadDueCardsForCategory(int spinnerPosition) {
        new Thread(() -> {
            long currentTime = System.currentTimeMillis();
            if (spinnerPosition == 0) {
                cardList = cardDao.getDueCards(currentTime);
            } else {
                int categoryId = categoryList.get(spinnerPosition - 1).getId();
                cardList = cardDao.getDueCardsByCategory(categoryId, currentTime);
            }

            currentCardIndex = 0;
            knownCount = 0;
            unknownCount = 0;
            repeatKnownCount = 0;
            repeatUnknownCount = 0;
            repeatCards.clear();

            runOnUiThread(() -> {
                if (cardList.isEmpty()) {
                    Toast.makeText(LearningActivity.this, "Keine fälligen Karten in dieser Kategorie", Toast.LENGTH_SHORT).show();
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
        Card currentCard = cardList.get(currentCardIndex);
        long now = System.currentTimeMillis();

        if (repeatModus) {
            if (knewIt) repeatKnownCount++;
            else repeatUnknownCount++;

            // Keine Statusänderung im Wiederholmodus

        } else {
            if (knewIt) {
                knownCount++;
                int newBox = Math.min(currentCard.getBox() + 1, 5);
                currentCard.setBox(newBox);

                int newInterval = Math.min(currentCard.getInterval() * 2, 64);
                currentCard.setInterval(newInterval);

                currentCard.setLastModified(now);
            } else {
                unknownCount++;
                repeatCards.add(currentCard);
                currentCard.setBox(1);
                currentCard.setInterval(1);
                currentCard.setLastModified(0);
            }
        }

        new Thread(() -> cardDao.updateCard(currentCard)).start();

        currentCardIndex++;

        if (currentCardIndex >= cardList.size()) {
            if (repeatModus) {
                String repeatSummary;
                if (repeatUnknownCount == 0) {
                    repeatSummary = "Du hast alle falschen Karten erfolgreich wiederholt!";
                } else {
                    repeatSummary = "Wiederholmodus: Du hast " + repeatKnownCount + " von " + cardList.size() +
                            " Karten gewusst.\nNicht gewusst: " + repeatUnknownCount;
                }

                new AlertDialog.Builder(this)
                        .setTitle("Wiederholungsresultat")
                        .setMessage(repeatSummary)
                        .setPositiveButton("OK", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();

            } else {
                String summary = "Du hast " + knownCount + " von " + cardList.size() + " Karten gewusst (" +
                        (cardList.size() > 0 ? (100 * knownCount / cardList.size()) : 0) + "%)." +
                        "\nNicht gewusst: " + unknownCount;

                if (!repeatCards.isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Lernfortschritt")
                            .setMessage(summary + "\n\nNicht gewusste Karten erneut lernen?")
                            .setPositiveButton("Ja", (dialog, which) -> {
                                repeatModus = true;
                                cardList = new ArrayList<>(repeatCards);
                                repeatCards.clear();
                                currentCardIndex = 0;
                                knownCount = 0;
                                unknownCount = 0;
                                repeatKnownCount = 0;
                                repeatUnknownCount = 0;
                                showCard(cardList.get(currentCardIndex));
                            })
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
            }
        } else {
            showCard(cardList.get(currentCardIndex));
        }
    }
}
