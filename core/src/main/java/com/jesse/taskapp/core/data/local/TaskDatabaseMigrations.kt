package com.jesse.taskapp.core.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room migrations for [TaskDatabase].
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN idempotencyKey TEXT")
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_tasks_idempotencyKey ON tasks(idempotencyKey)",
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
    }
}
