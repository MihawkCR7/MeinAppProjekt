package com.example.flashcardapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcardapp.model.Card;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<Card> cardList;

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
        this.cardList = cardList;
    }

    public void setCardList(List<Card> cards) {
        this.cardList = cards;
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
        Card card = cardList.get(position);
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
        return cardList != null ? cardList.size() : 0;
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
