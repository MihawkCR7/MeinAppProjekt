package com.example.flashcardapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.stream.Collectors;

public class EditCardActivity extends AppCompatActivity {

    private EditText questionEditText;
    private EditText answerEditText;
    private Spinner categorySpinner;
    private Button saveButton;
    private Button deleteButton;

    private AppDatabase db;
    private CardDao cardDao;
    private CategoryDao categoryDao;

    private Card loadedCard = null;
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_card);

        questionEditText = findViewById(R.id.editText_question);
        answerEditText = findViewById(R.id.editText_answer);
        categorySpinner = findViewById(R.id.spinner_category);
        saveButton = findViewById(R.id.button_save);
        deleteButton = findViewById(R.id.button_delete);

        db = AppDatabase.getDatabase(getApplicationContext());
        cardDao = db.cardDao();
        categoryDao = db.categoryDao();

        int cardId = getIntent().getIntExtra("card_id", -1);

        loadCategories(() -> {
            if (cardId != -1) {
                loadCard(cardId);
            } else {
                deleteButton.setEnabled(false);
            }
        });

        saveButton.setOnClickListener(v -> saveCard());

        deleteButton.setOnClickListener(v -> {
            if (loadedCard != null) {
                new AlertDialog.Builder(EditCardActivity.this)
                        .setTitle("Karte löschen")
                        .setMessage("Willst du diese Karte wirklich löschen?")
                        .setPositiveButton("Ja", (dialog, which) -> {
                            new Thread(() -> {
                                cardDao.deleteCard(loadedCard);
                                runOnUiThread(() -> {
                                    Toast.makeText(EditCardActivity.this, "Karte gelöscht", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }).start();
                        })
                        .setNegativeButton("Nein", null)
                        .show();
            }
        });
    }

    private void loadCategories(Runnable onLoaded) {
        new Thread(() -> {
            categories = categoryDao.getAllCategories();
            runOnUiThread(() -> {
                List<String> categoryNames = categories.stream()
                        .map(Category::getName)
                        .collect(Collectors.toList());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);

                onLoaded.run();
            });
        }).start();
    }

    private void loadCard(int cardId) {
        new Thread(() -> {
            loadedCard = cardDao.getAllCards().stream()
                    .filter(card -> card.getId() == cardId)
                    .findFirst()
                    .orElse(null);

            if (loadedCard != null) {
                runOnUiThread(() -> {
                    questionEditText.setText(loadedCard.getQuestion());
                    answerEditText.setText(loadedCard.getAnswer());
                    deleteButton.setEnabled(true);

                    // Kategorie im Spinner auswählen
                    int categoryIndex = 0;
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).getId() == loadedCard.getCategoryId()) {
                            categoryIndex = i;
                            break;
                        }
                    }
                    categorySpinner.setSelection(categoryIndex);
                });
            }
        }).start();
    }

    private void saveCard() {
        String question = questionEditText.getText().toString().trim();
        String answer = answerEditText.getText().toString().trim();

        if (question.isEmpty() || answer.isEmpty()) {
            Toast.makeText(this, "Bitte Frage und Antwort eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();

        int selectedCategoryIndex = categorySpinner.getSelectedItemPosition();
        int selectedCategoryId = categories.isEmpty() || selectedCategoryIndex < 0
                ? 0
                : categories.get(selectedCategoryIndex).getId();

        if (loadedCard == null) {
            Card newCard = new Card();
            newCard.setQuestion(question);
            newCard.setAnswer(answer);
            newCard.setBox(1);
            newCard.setLastModified(now);
            newCard.setCategoryId(selectedCategoryId);

            new Thread(() -> {
                cardDao.insertCard(newCard);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Karte gespeichert", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        } else {
            loadedCard.setQuestion(question);
            loadedCard.setAnswer(answer);
            loadedCard.setLastModified(now);
            loadedCard.setCategoryId(selectedCategoryId);

            new Thread(() -> {
                cardDao.updateCard(loadedCard);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Karte aktualisiert", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        }
    }
}
