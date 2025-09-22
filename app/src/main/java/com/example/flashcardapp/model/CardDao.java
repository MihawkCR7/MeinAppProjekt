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

    @Insert
    void insertCard(Card card);

    @Update
    void updateCard(Card card);

    @Delete
    void deleteCard(Card card);
}
