package com.example.flashcardapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "cards",
        foreignKeys = @ForeignKey(entity = Category.class,
                parentColumns = "id",
                childColumns = "category_id",
                onDelete = CASCADE))
public class Card {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String question;
    private String answer;

    // Leitner Box Status (z.B. 1 bis 5)
    private int box;

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
