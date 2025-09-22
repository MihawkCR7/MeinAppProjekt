package com.example.flashcardapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcardapp.model.Card;

import java.util.ArrayList;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<Card> fullCardList;     // Alle Karten (Original)
    private List<Card> filteredCardList; // Gefilterte Karten (Anzeige)

    // Interface für Karten-Klick
    public interface OnCardClickListener {
        void onCardClick(Card card);
    }

    private OnCardClickListener listener;

    // Setter für den Listener
    public void setOnCardClickListener(OnCardClickListener listener) {
        this.listener = listener;
    }

    public CardAdapter(List<Card> cardList) {
        this.fullCardList = new ArrayList<>(cardList);
        this.filteredCardList = new ArrayList<>(cardList);
    }

    public void setCardList(List<Card> cards) {
        this.fullCardList.clear();
        this.fullCardList.addAll(cards);
        filter(""); // Filter zurücksetzen, alle anzeigen
    }

    // Neue Filtermethode
    public void filter(String text) {
        filteredCardList.clear();
        if (text == null || text.isEmpty()) {
            filteredCardList.addAll(fullCardList);
        } else {
            String lowerText = text.toLowerCase();
            for (Card card : fullCardList) {
                if (card.getQuestion().toLowerCase().contains(lowerText) ||
                        card.getAnswer().toLowerCase().contains(lowerText)) {
                    filteredCardList.add(card);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item, parent, false);
        return new CardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = filteredCardList.get(position);
        holder.questionTextView.setText(card.getQuestion());
        holder.answerTextView.setText(card.getAnswer());
        holder.answerTextView.setVisibility(View.GONE);

        // Umschalten Frage/Antwort bei Klick auf Frage
        holder.questionTextView.setOnClickListener(v -> {
            if (holder.answerTextView.getVisibility() == View.GONE) {
                holder.answerTextView.setVisibility(View.VISIBLE);
            } else {
                holder.answerTextView.setVisibility(View.GONE);
            }
        });

        // Gesamten Karteneintrag klickbar machen für externen Listener (Bearbeiten)
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onCardClick(card);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return filteredCardList != null ? filteredCardList.size() : 0;
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView;
        TextView answerTextView;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.card_question);
            answerTextView = itemView.findViewById(R.id.card_answer);
        }
    }
}
