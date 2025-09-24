package com.example.flashcardapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "cards",
        indices = {@Index(value = {"category_id"})},
        foreignKeys = @ForeignKey(entity = Category.class,
                parentColumns = "id",
                childColumns = "category_id",
                onDelete = CASCADE))
public class Card {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String question;
    private String answer;

    // Leitner Box Status (Default 1)
    private int box = 1;

    @ColumnInfo(name = "interval_days")
    private int interval = 1; // Anzahl Tage bis zur nächsten Wiederholung, Default 1

    @ColumnInfo(name = "last_modified")
    private long lastModified;

    @ColumnInfo(name = "category_id")
    private int categoryId;

    // Getter und Setter

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }
    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public int getBox() {
        return box;
    }
    public void setBox(int box) {
        this.box = box;
    }
    public int getInterval() {
        return interval;
    }
    public void setInterval(int interval) {
        this.interval = interval;
    }
    public long getLastModified() {
        return lastModified;
    }
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    public int getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
}
