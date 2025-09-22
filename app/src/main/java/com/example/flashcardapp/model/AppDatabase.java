package com.example.flashcardapp.model;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;

@Database(entities = {Card.class, Category.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CardDao cardDao();
    public abstract CategoryDao categoryDao();

    private static AppDatabase INSTANCE;

    // Beispielmigration von Version 1 auf 3, passe bei Bedarf an deine vorherigen Versionen an
    private static final Migration MIGRATION_1_3 = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Beispiel: Neue Spalte category_id in Tabelle cards hinzufügen mit Defaultwert 0
            database.execSQL("ALTER TABLE cards ADD COLUMN category_id INTEGER NOT NULL DEFAULT 0");
            // Index auf category_id für Performance
            database.execSQL("CREATE INDEX index_cards_category_id ON cards(category_id)");
        }
    };

    public static synchronized AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            // Entferne das Löschen der Datenbank beim Start
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "flashcard_database")
                    // Füge die Migration ein und entferne fallbackToDestructiveMigration
                    .addMigrations(MIGRATION_1_3)
                    .build();
        }
        return INSTANCE;
    }
}
