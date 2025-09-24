package com.example.flashcardapp.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CardDao {

    @Query("SELECT * FROM cards")
    List<Card> getAllCards();

    @Query("SELECT * FROM cards ORDER BY last_modified DESC")
    List<Card> getAllCardsSorted();

    @Query("SELECT * FROM cards WHERE category_id = :categoryId ORDER BY last_modified DESC")
    List<Card> getCardsByCategory(int categoryId);

    @Query("SELECT * FROM cards WHERE (last_modified + (interval_days * 86400000) <= :now) OR last_modified = 0 ORDER BY id ASC")
    List<Card> getDueCards(long now);

    @Query("SELECT * FROM cards WHERE (category_id = :categoryId) AND ((last_modified + (interval_days * 86400000) <= :now) OR last_modified = 0) ORDER BY id ASC")
    List<Card> getDueCardsByCategory(int categoryId, long now);

    @Insert
    void insertCard(Card card);

    @Update
    void updateCard(Card card);

    @Delete
    void deleteCard(Card card);
}
