package com.jesse.taskapp.core.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import android.content.Context
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@RunWith(AndroidJUnit4::class)
class TaskDatabaseMigrationTest {

    private val context: Context =
        ApplicationProvider.getApplicationContext()

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TaskDatabase::class.java,
    )

    @Test
    fun migrate3To4_preservesTasksAndCreatesPendingOperationsTable() {
        val dbName = "migration-test"

        // Create a database using the exported v3 schema.
        helper.createDatabase(dbName, 3).apply {
            execSQL(
                """
                INSERT INTO tasks (
                    id,
                    title,
                    description,
                    createdAtMillis,
                    updatedAtMillis,
                    idempotencyKey,
                    version
                ) VALUES (
                    1,
                    'Buy milk',
                    'From the supermarket',
                    100,
                    200,
                    'create-task-123',
                    2
                )
                """,
            )
            close()
        }

        // Apply the v3 -> v4 migration.
        val migratedDb = helper.runMigrationsAndValidate(
            dbName,
            4,
            true,
            MIGRATION_3_4,
        )

        // Verify the existing task survived.
        migratedDb.query(
            "SELECT * FROM tasks WHERE id = 1",
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Buy milk", cursor.getString(cursor.getColumnIndexOrThrow("title")))
            assertEquals(
                "create-task-123",
                cursor.getString(cursor.getColumnIndexOrThrow("idempotencyKey")),
            )
            assertEquals(
                2L,
                cursor.getLong(cursor.getColumnIndexOrThrow("version")),
            )
        }

        // Verify the new table exists.
        migratedDb.query(
            """
            SELECT name FROM sqlite_master
            WHERE type = 'table'
            AND name = 'pending_operations'
            """,
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
        }

        migratedDb.close()
    }
}