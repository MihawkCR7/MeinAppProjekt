package com.example.flashcardapp.model;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;

@Database(entities = {Card.class, Category.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CardDao cardDao();
    public abstract CategoryDao categoryDao();

    private static AppDatabase INSTANCE;

    // Migration von Version 1 auf 3 (bestehend)
    private static final Migration MIGRATION_1_3 = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE cards ADD COLUMN category_id INTEGER NOT NULL DEFAULT 0");
            database.execSQL("CREATE INDEX index_cards_category_id ON cards(category_id)");
        }
    };

    // Migration von Version 3 auf 4: Spalte box mit Default 1 hinzufügen
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE cards ADD COLUMN box INTEGER NOT NULL DEFAULT 1");
        }
    };

    // Migration von Version 4 auf 5: Spalte interval_days mit Default 1 hinzufügen
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE cards ADD COLUMN interval_days INTEGER NOT NULL DEFAULT 1");
        }
    };

    public static synchronized AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "flashcard_database")
                    .addMigrations(MIGRATION_1_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build();
        }
        return INSTANCE;
    }
}
