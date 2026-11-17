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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pending_operations (
                id TEXT NOT NULL,
                type TEXT NOT NULL,
                taskId INTEGER NOT NULL,
                expectedTaskVersion INTEGER NOT NULL,
                taskTitleSnapshot TEXT NOT NULL,
                status TEXT NOT NULL,
                requestedAtMillis INTEGER NOT NULL,
                expiresAtMillis INTEGER NOT NULL,
                consentGrantedAtMillis INTEGER,
                completedAtMillis INTEGER,
                updatedAtMillis INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """,
        )

        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_pending_operations_status ON pending_operations(status)",
        )

        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_pending_operations_taskId ON pending_operations(taskId)",
        )
    }
}
